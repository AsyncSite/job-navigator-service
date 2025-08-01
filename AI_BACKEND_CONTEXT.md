# Job Navigator Service - AI 개발자를 위한 핵심 컨텍스트

> 이 문서는 Job Navigator Service를 개발하는 다음 AI를 위한 가장 중요한 문서입니다.
> 작업 시작 전 반드시 이 문서를 완독하고 시작하세요.

## 🚨 가장 먼저 알아야 할 것

### 1. Docker 이미지 재빌드 필수
**이것을 모르면 몇 시간을 낭비합니다!**
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
4. **Redis 캐싱** - 78% 성능 개선 (402ms → 89ms)
5. **TechStack 관계** - Many-to-Many 관계 매핑 완료

### ❌ 미해결 이슈
1. **캐싱 로직 버그** - 필터링 전 결과를 캐시해서 필터가 작동 안함 (임시 비활성화)
2. **TechStack 필터링** - 코드는 추가했으나 Docker 재빌드 후 테스트 필요
3. **입력값 검증** - 음수 페이지 번호 등 엣지 케이스 처리 부족

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

## 다음 AI가 해야 할 작업 (우선순위 순)

### 1. 🔥 캐싱 로직 수정 (긴급)
**문제**: JobService의 캐시가 필터링 전 결과를 저장

**해결 방향**:
```java
// 현재 (잘못됨)
cachePort.cacheSearchResult(cacheKey, pagedJobs); // 페이징된 일부만 저장

// 수정 필요
// 1. SearchJobsResult 전체를 캐시하도록 변경
// 2. 또는 필터링된 전체 결과를 저장하고 페이징은 캐시에서 처리
```

### 2. TechStack 필터링 테스트
이미 코드는 추가됨:
```java
// JobService.java (93-118줄)
.filter(job -> {
    if (command.techStackIds() != null && !command.techStackIds().isEmpty()) {
        // ... 필터링 로직
    }
})
```
Docker 재빌드 후 테스트 필요

### 3. 입력값 검증 추가
```java
// JobService.searchJobs()
if (command.page() < 0) {
    throw new IllegalArgumentException("Page number cannot be negative");
}
if (command.size() > 100) {
    command = command.withSize(100); // 최대 100개로 제한
}
```

### 4. 프론트엔드 검색 디바운싱
```typescript
// NavigatorPage.tsx
const debouncedSearch = useMemo(
    () => debounce((query: string) => {
        setSearchQuery(query);
    }, 300),
    []
);
```

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

1. 캐싱 로직 재설계 (HIGH)
2. 입력값 검증 추가 (HIGH)  
3. 프론트엔드 검색 디바운싱 (MEDIUM)
4. 매칭 점수 계산 로직 (LOW)

---

**작성일**: 2025-08-01  
**마지막 수정**: TechStack 필터링 코드 추가, 캐시 임시 비활성화