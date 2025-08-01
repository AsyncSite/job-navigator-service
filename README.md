# Job Navigator Service

AsyncSite의 개발자 채용 공고 수집 및 매칭 서비스의 메인 백엔드 서비스입니다.

## 기술 스택

- Java 21
- Spring Boot 3.5.3
- Spring Data JPA
- MySQL 8.0
- Redis
- Docker
- Spring Cloud Netflix Eureka

## 아키텍처

Clean Architecture (Hexagonal Architecture) 패턴을 따릅니다:

```
src/main/java/com/asyncsite/jobnavigator/
├── adapter/
│   ├── in/
│   │   └── web/           # REST Controllers
│   └── out/
│       ├── persistence/   # JPA Repositories
│       └── client/        # External Service Clients
├── application/
│   ├── port/
│   │   ├── in/           # Use Cases
│   │   └── out/          # Port Interfaces
│   ├── service/          # Service Implementations
│   └── dto/              # DTOs
└── domain/               # Domain Models (Pure Java)
```

## 로컬 개발 환경 설정

### 필수 요구사항

- JDK 21
- Docker & Docker Compose
- Gradle

### 빌드 및 실행

1. **애플리케이션 빌드**
   ```bash
   ./gradlew clean build
   ```

2. **Docker 이미지 빌드**
   ```bash
   ./build-docker.sh
   ```

3. **전체 스택 실행** (인프라 포함)
   ```bash
   docker-compose up -d
   ```

4. **서비스만 실행** (인프라가 이미 실행 중인 경우)
   ```bash
   docker-compose -f docker-compose.job-navigator-only.yml up -d
   ```

## API 엔드포인트

### 공고 관련 API

- `GET /api/jobs` - 공고 목록 조회 (페이징, 필터링)
- `GET /api/jobs/{id}` - 공고 상세 조회
- `POST /api/jobs/{id}/save` - 관심 공고 저장
- `GET /api/jobs/my/saved` - 저장된 공고 목록

### 검색/필터 API

- `GET /api/jobs/companies` - 회사 목록
- `GET /api/jobs/tech-stacks` - 기술 스택 목록

### 크롤러 연동 API (내부용)

- `POST /api/jobs/batch` - 크롤러 데이터 수신

## 환경 변수

### 필수 환경 변수

- `SPRING_DATASOURCE_URL` - MySQL 연결 URL
- `SPRING_DATASOURCE_USERNAME` - MySQL 사용자명
- `SPRING_DATASOURCE_PASSWORD` - MySQL 비밀번호
- `SPRING_DATA_REDIS_HOST` - Redis 호스트
- `SPRING_DATA_REDIS_PORT` - Redis 포트
- `EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE` - Eureka 서버 URL

## 모니터링

- Health Check: `http://localhost:12085/actuator/health`
- Metrics: `http://localhost:12085/actuator/metrics`
- Swagger UI: `http://localhost:12085/swagger-ui.html`

## CI/CD

- **CI**: GitHub Actions를 통한 자동 테스트 및 빌드
- **CD**: Docker Hub로 자동 배포

## 개발 가이드

1. 도메인 모델은 Spring 의존성 없이 순수 Java로 작성
2. JPA Entity와 Domain 모델은 반드시 분리
3. 모든 외부 의존성은 Port/Adapter 패턴으로 격리
4. 테스트 커버리지 80% 이상 유지