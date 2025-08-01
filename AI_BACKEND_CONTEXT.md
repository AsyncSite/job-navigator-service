# Job Navigator Service - AI 개발자를 위한 핵심 컨텍스트

> 이 문서는 Job Navigator Service를 개발하는 다음 AI를 위한 가장 중요한 문서입니다.
> 작업 시작 전 반드시 이 문서를 완독하고 시작하세요.

## 🚨 가장 먼저 알아야 할 것

### 1. Docker 이미지 재빌드 필수
**이것을 모르면 몇 시간을 낭비합니다!**

#### 🚀 가장 쉬운 방법 (권장)
```bash
# 한 번에 clean build + Docker 재빌드 + 재시작
./gradlew rebuild
```

#### 수동으로 하는 방법
```bash
# 코드 변경 후 반드시 실행
./gradlew clean build -x test
docker-compose -f docker-compose.job-navigator-only.yml build job-navigator-service
docker-compose -f docker-compose.job-navigator-only.yml up -d job-navigator-service
```
❌ `docker-compose restart`만 하면 변경사항이 반영되지 않음!

### 2. Redis 캐시가 테스트를 방해함
```bash
# 테스트 전 항상 실행
docker exec asyncsite-redis redis-cli FLUSHALL
```

### 3. 프로젝트 구조
```
/asyncsite/
├── job-navigator-service/     # 메인 백엔드 (Java 21, Spring Boot 3.5.3)
├── web/                       # 프론트엔드 (React, TypeScript)
├── core-platform/            
│   ├── gateway/              # API Gateway (포트 8080)
│   └── security/             # 보안 설정 (Kotlin)
└── docker-compose.job-navigator-only.yml
```

## 프로젝트 현재 상태 (2025-08-01)

### ✅ 구현 완료
1. **핵심 API** - 모든 CRUD 및 검색 API 구현 완료
2. **프론트엔드 연동** - Navigator 페이지가 백엔드 API 사용 중
3. **Gateway 통합** - `/api/job-navigator/**` 라우팅 작동
4. **Redis 캐싱** - 68% 성능 개선 (222ms → 71ms)
5. **TechStack 관계** - Many-to-Many 관계 매핑 완료
6. **상세보기 기능** - JobDetailModal 구현 완료
7. **Mock 데이터** - 10개 채용공고 및 15개 회사 데이터
8. **입력값 검증** - 페이지 번호, 크기, 키워드 길이 검증 완료
9. **CI/CD 빌드** - 누락된 31개 파일 모두 추가 완료
10. **프로덕션 DB 생성** - MySQL init 스크립트 추가로 자동 생성 가능

### ✅ 해결된 이슈
1. **캐싱 로직 수정** - 필터링된 결과를 캐시하도록 수정 완료
2. **TechStack 필터링** - 테스트 완료 및 정상 작동
3. **프로덕션 DB 자동 생성** - MySQL init 스크립트 사용

## 해결된 주요 문제들 (시행착오 포함)

### 1. Redis LocalDateTime 역직렬화 오류
**증상**: `java.time.LocalDateTime not supported by default`

**해결**:
```java
// RedisConfig.java
@Bean(name = "redisObjectMapper")
public ObjectMapper redisObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule()); // 이게 핵심!
    return objectMapper;
}

// Job.java
@JsonDeserialize(builder = Job.JobBuilder.class)
@JsonPOJOBuilder(withPrefix = "")
```

### 2. TechStack이 API 응답에 없음
**시행착오**:
1. 처음엔 로그만 찍어봄 → DB에는 있음
2. Lazy Loading 문제인줄 알고 Fetch Join 추가 → 여전히 안됨
3. saveJob 메서드 확인 → TechStack 저장 로직이 아예 없었음!

**해결**: JobPersistenceAdapter.saveJob()에 저장 로직 추가

### 3. Gateway 401 오류
**해결**: SecurityConfig에 공개 경로 추가
```kotlin
// core-platform/security/.../SecurityConfig.kt
PathPatternParserServerWebExchangeMatcher("/api/job-navigator/**"),
```

### 4. 프로덕션 DB 자동 생성 실패
**증상**: 프로덕션 환경에서 "Unknown database 'job_db'" 오류 발생

**원인**: `createDatabaseIfNotExist=true`는 보안상 프로덕션에서 작동하지 않음

**해결**: MySQL 초기화 스크립트 추가
```sql
-- mysql/init/01-create-job-databases.sql
CREATE DATABASE IF NOT EXISTS job_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS job_db_test CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

GRANT ALL PRIVILEGES ON job_db.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON job_db_test.* TO 'root'@'%';
FLUSH PRIVILEGES;
```

**docker-compose.yml 수정**:
```yaml
mysql:
  volumes:
    - asyncsite-mysql-data:/var/lib/mysql
    - ./mysql/init:/docker-entrypoint-initdb.d  # 이 라인 추가
```

## 🎯 다음 작업: 크롤러 서비스 구현

> **현재 상태**: 웹-백엔드 연동이 완료되었고, Mock 데이터로 전체 흐름이 검증되었습니다.
> 이제 실제 채용 데이터를 수집하는 크롤러 서비스를 구현할 차례입니다.

### 🚀 크롤러 서비스 구현 가이드

#### 1. 서비스 생성 및 기본 구조
```bash
# 프로젝트 루트에서 실행
cd /Users/Rene/Documents/rene/project/asyncsite
mkdir job-crawler-service
cd job-crawler-service
```

**Python 프로젝트 구조** (backend_design_v1.md 참조):
```
job-crawler-service/
├── src/
│   ├── adapter/
│   │   ├── inbound/
│   │   │   ├── api/          # FastAPI 컨트롤러
│   │   │   └── scheduler/    # 크롤링 스케줄러
│   │   └── outbound/
│   │       ├── crawler/      # 사이트별 크롤러
│   │       └── client/       # Job Navigator Service 클라이언트
│   ├── application/
│   │   ├── port/            # 인터페이스 정의
│   │   ├── service/         # 비즈니스 로직
│   │   └── dto/             # 데이터 전송 객체
│   └── domain/              # 도메인 모델
├── tests/
├── requirements.txt
├── Dockerfile
└── docker-compose.yml
```

#### 2. 기술 스택 및 의존성
**requirements.txt**:
```
fastapi==0.109.2
uvicorn[standard]==0.27.0
httpx==0.26.0
beautifulsoup4==4.12.3
lxml==5.1.0
apscheduler==3.10.4
pydantic==2.5.3
pydantic-settings==2.1.0
structlog==24.1.0
prometheus-client==0.19.0
pytest==7.4.4
pytest-asyncio==0.23.3
```

#### 3. 핵심 구현 사항

##### 3.1 타겟 사이트 분석 (우선순위 순)
1. **네이버 커리어** (careers.naver.com)
   - RESTful API 존재 여부 확인
   - HTML 구조 분석
   - 페이지네이션 방식

2. **카카오** (careers.kakao.com)
3. **라인** (careers.linecorp.com/ko)
4. **쿠팡** (www.coupang.jobs)
5. **배달의민족** (career.woowahan.com)

##### 3.2 크롤러 베이스 클래스
```python
# src/adapter/outbound/crawler/base_crawler.py
from abc import ABC, abstractmethod
from typing import List
import httpx
import asyncio
import random
from bs4 import BeautifulSoup

class BaseCrawler(ABC):
    def __init__(self, company_name: str):
        self.company_name = company_name
        self.client = httpx.AsyncClient(
            headers={
                'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
                'Accept-Language': 'ko-KR,ko;q=0.9,en;q=0.8'
            },
            timeout=30.0
        )
    
    @abstractmethod
    async def fetch_job_list(self) -> List[str]:
        """채용공고 URL 목록 수집"""
        pass
    
    @abstractmethod
    async def parse_job_detail(self, url: str) -> dict:
        """채용공고 상세 정보 파싱"""
        pass
    
    async def crawl(self) -> List[dict]:
        """메인 크롤링 로직"""
        jobs = []
        job_urls = await self.fetch_job_list()
        
        for url in job_urls[:10]:  # 초기엔 10개만 테스트
            await asyncio.sleep(random.uniform(1, 2))  # Rate limiting
            job = await self.parse_job_detail(url)
            if job:
                jobs.append(job)
        
        return jobs
```

##### 3.3 Job Navigator Service 클라이언트
```python
# src/adapter/outbound/client/job_service_client.py
import httpx
from typing import List, Dict

class JobServiceClient:
    def __init__(self, base_url: str):
        self.base_url = base_url
        self.client = httpx.AsyncClient()
    
    async def send_jobs(self, jobs: List[Dict]) -> dict:
        """크롤링한 채용공고를 Job Navigator Service로 전송"""
        response = await self.client.post(
            f"{self.base_url}/api/jobs/batch",
            json=jobs
        )
        return response.json()
```

##### 3.4 스케줄러 설정
```python
# src/adapter/inbound/scheduler/crawl_scheduler.py
from apscheduler.schedulers.asyncio import AsyncIOScheduler
from datetime import datetime

class CrawlScheduler:
    def __init__(self, crawl_service):
        self.scheduler = AsyncIOScheduler()
        self.crawl_service = crawl_service
    
    def start(self):
        # 매일 새벽 3시 실행
        self.scheduler.add_job(
            self.crawl_service.crawl_all_companies,
            'cron',
            hour=3,
            minute=0
        )
        
        # 테스트용: 5분마다 실행
        # self.scheduler.add_job(
        #     self.crawl_service.crawl_all_companies,
        #     'interval',
        #     minutes=5
        # )
        
        self.scheduler.start()
```

#### 4. 구현 순서 및 체크리스트

**Week 1: 기반 구축**
- [ ] Python 프로젝트 구조 생성
- [ ] FastAPI 기본 설정
- [ ] 도메인 모델 정의
- [ ] 크롤러 베이스 클래스 구현
- [ ] Job Service 클라이언트 구현

**Week 2: 첫 번째 크롤러 구현**
- [ ] 네이버 채용 사이트 분석
- [ ] 네이버 크롤러 구현
- [ ] 파싱 로직 구현 (BeautifulSoup)
- [ ] 기술 스택 추출 로직
- [ ] 테스트 작성

**Week 3: 추가 크롤러 및 스케줄링**
- [ ] 카카오 크롤러 구현
- [ ] 배달의민족 크롤러 구현
- [ ] 스케줄러 구현
- [ ] 에러 처리 및 재시도 로직
- [ ] 크롤링 로그 저장

**Week 4: 통합 및 배포**
- [ ] Docker 이미지 빌드
- [ ] docker-compose 통합
- [ ] 모니터링 설정 (Prometheus)
- [ ] 알림 설정 (실패 시)
- [ ] 운영 배포

#### 5. 중요 고려사항

##### 5.1 robots.txt 준수
```python
async def check_robots_txt(self, url: str) -> bool:
    """robots.txt 확인"""
    # 구현 필요
    pass
```

##### 5.2 Rate Limiting
- 요청 간 1-2초 딜레이
- 동시 요청 수 제한 (최대 3개)
- 429 응답 시 백오프

##### 5.3 에러 처리
- 네트워크 오류: 3회 재시도
- 파싱 오류: 로그 남기고 건너뛰기
- 구조 변경 감지: 알림 발송

##### 5.4 데이터 검증
```python
def validate_job_data(job: dict) -> bool:
    """필수 필드 검증"""
    required_fields = ['title', 'company_name', 'source_url']
    return all(field in job and job[field] for field in required_fields)
```

#### 6. 테스트 전략

##### 6.1 단위 테스트
```python
# tests/test_naver_crawler.py
import pytest
from src.adapter.outbound.crawler.naver_crawler import NaverCrawler

@pytest.mark.asyncio
async def test_parse_job_detail():
    crawler = NaverCrawler()
    # Mock HTML 데이터로 테스트
    pass
```

##### 6.2 통합 테스트
- 실제 사이트 크롤링 (소량)
- Job Service 연동 테스트
- 전체 플로우 테스트

#### 7. 모니터링 및 알림

##### 7.1 메트릭 수집
```python
from prometheus_client import Counter, Histogram

crawl_jobs_total = Counter(
    'crawler_jobs_total',
    'Total number of jobs crawled',
    ['company', 'status']
)

crawl_duration = Histogram(
    'crawler_duration_seconds',
    'Time spent crawling',
    ['company']
)
```

##### 7.2 로깅
```python
import structlog

logger = structlog.get_logger()

logger.info(
    "crawl_started",
    company=company_name,
    timestamp=datetime.utcnow().isoformat()
)
```

### 📋 Job Navigator Service 수정 사항

#### 1. 배치 저장 API 구현
```java
// JobWebAdapter.java
@PostMapping("/batch")
@ResponseStatus(HttpStatus.CREATED)
public BatchSaveResponse saveBatch(@RequestBody List<SaveJobCommand> commands) {
    // 구현 필요
}
```

#### 2. 크롤링 로그 저장
- CrawlLog 엔티티는 이미 존재
- 저장 로직만 구현 필요

### 🎯 성공 기준

1. **최소 3개 사이트** 크롤링 성공
2. **매일 100개 이상** 신규 채용공고 수집
3. **중복 제거** 정확도 95% 이상
4. **기술 스택 추출** 정확도 80% 이상
5. **안정성**: 일주일 연속 무장애 운영

## 유용한 명령어 모음

### 개발 중 자주 사용
```bash
# 로그 실시간 확인
docker logs -f asyncsite-job-navigator --tail 100

# DB 데이터 확인
docker exec -i asyncsite-mysql mysql -u root -pasyncsite_root_2024! job_db -e "SELECT * FROM job_postings"

# API 테스트
curl -s "http://localhost:8080/api/job-navigator/jobs" | jq '.'

# 캐시 비우기
docker exec asyncsite-redis redis-cli FLUSHALL

# 서비스 상태 확인
docker ps | grep -E "(gateway|job-navigator|mysql|redis)"
```

### 문제 해결 시
```bash
# Gateway 라우팅 확인
curl -s "http://localhost:8080/actuator/gateway/routes" | jq '.[] | select(.id == "job-navigator-service")'

# 직접 vs Gateway 테스트
curl -s "http://localhost:12085/jobs"  # 직접
curl -s "http://localhost:8080/api/job-navigator/jobs"  # Gateway

# 로그에서 에러 찾기
docker logs asyncsite-job-navigator 2>&1 | grep -E "(ERROR|WARN|Exception)"
```

### Docker 작업 명령어 (Gradle Tasks)
```bash
# 🚀 가장 많이 사용하는 명령어
./gradlew rebuild                      # clean build + Docker 재빌드 + 재시작 (권장!)

# Job Navigator 전용 명령어
./gradlew dockerBuildJobNavigator      # Docker 이미지 빌드만
./gradlew dockerUpJobNavigator         # Job Navigator 시작
./gradlew dockerDownJobNavigator       # Job Navigator 중지
./gradlew dockerRestartJobNavigator    # Job Navigator 재시작
./gradlew dockerLogsJobNavigator       # 로그 실시간 확인
./gradlew rebuildJobNavigator          # rebuild와 동일 (full name)

# 도움말
./gradlew dockerHelpJobNavigator       # 모든 Docker 명령어 보기

# 기존 명령어 (여전히 작동)
./gradlew dockerUp                     # 전체 스택 시작
./gradlew dockerDown                   # 전체 스택 중지
```

## 파일 위치 빠른 참조

### 수정이 자주 필요한 파일
- **JobService.java**: `/job-navigator-service/src/main/java/com/asyncsite/jobnavigator/application/service/JobService.java`
- **application.yml**: `/job-navigator-service/src/main/resources/application.yml`
- **JobPersistenceAdapter.java**: `.../adapter/out/persistence/JobPersistenceAdapter.java`

### 프론트엔드 연동 파일
- **API 클라이언트**: `/web/src/api/jobNavigatorService.ts`
- **Navigator 페이지**: `/web/src/pages/ignition/navigator/NavigatorPage.tsx`

### 설정 파일
- **Gateway 라우팅**: `/core-platform/gateway/src/main/resources/application.yml`
- **Security 설정**: `/core-platform/security/src/main/kotlin/.../SecurityConfig.kt`

## 필수 읽어야 할 다른 문서

1. **`docs/backend_design_v1.md`** - 전체 시스템 설계와 구현 상태
2. **`docs/frontend-backend-integration-analysis.md`** - 프론트엔드 연동 현황
3. **`docs/TESTING_GUIDELINE.md`** - 테스트 방법과 주의사항

## 데이터베이스 정보

- Database: `job_db`
- 주요 테이블:
  - `job_postings` - 채용공고
  - `companies` - 회사 정보
  - `tech_stacks` - 기술 스택
  - `job_tech_stacks` - 공고-기술 관계 (Many-to-Many)
  - `user_saved_jobs` - 사용자 저장 공고

## 마지막 조언

1. **항상 Docker 이미지 재빌드** - 이거 안 하면 왜 안 되는지 몇 시간 고민하게 됨
2. **캐시를 의심하라** - 이상하면 먼저 Redis 비우고 테스트
3. **로그를 믿지 말고 DB를 직접 확인** - JPA가 예상과 다르게 동작할 수 있음
4. **Gateway와 직접 호출 둘 다 테스트** - 문제 격리에 도움
5. **사용자의 피드백 존중** - "think hard and think big"이라고 하면 더 꼼꼼히

## 현재 TODO 우선순위

1. ~~캐싱 로직 재설계~~ ✅ 완료
2. ~~입력값 검증 추가~~ ✅ 완료  
3. ~~프로덕션 DB 자동 생성~~ ✅ 완료
4. **크롤러 서비스 구현** (CRITICAL) 🔥 - 다음 AI가 진행해야 할 작업
5. 프론트엔드 검색 디바운싱 (MEDIUM)
6. 매칭 점수 계산 로직 (LOW)

---

**작성일**: 2025-08-01  
**마지막 수정**: 2025-08-01 - 프로덕션 DB 자동 생성 이슈 해결, 모든 기능 정상 작동 확인