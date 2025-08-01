# Job Navigator Service 테스트 가이드라인

> 이 문서는 Job Navigator Service를 테스트하면서 겪은 시행착오와 해결 방법을 정리한 것입니다.
> 다음 개발자가 동일한 실수를 반복하지 않도록 작성되었습니다.

## 🚨 가장 중요한 주의사항

### 1. Docker 이미지 재빌드 필수
**문제**: 코드를 수정하고 `docker-compose restart`만 하면 변경사항이 반영되지 않습니다.

**이유**: restart는 기존 컨테이너를 재시작할 뿐, 새로운 JAR 파일을 사용하지 않습니다.

**올바른 방법**:
```bash
# 1. 프로젝트 빌드
./gradlew clean build -x test

# 2. Docker 이미지 재빌드 (이 단계가 핵심!)
docker-compose -f docker-compose.job-navigator-only.yml build job-navigator-service

# 3. 컨테이너 재생성
docker-compose -f docker-compose.job-navigator-only.yml up -d job-navigator-service
```

### 2. Redis 캐시 때문에 테스트 결과가 왜곡될 수 있음
**문제**: API를 테스트할 때 이전 결과가 캐시되어 있어 필터링이나 수정사항이 반영되지 않음

**해결 방법**:
```bash
# 테스트 전 캐시 비우기
docker exec asyncsite-redis redis-cli FLUSHALL

# 또는 캐시 키 확인 후 특정 키만 삭제
docker exec asyncsite-redis redis-cli KEYS "*"
docker exec asyncsite-redis redis-cli DEL "search:..."
```

## 테스트 방법론

### 1. 서비스 상태 확인
```bash
# 필수 서비스 확인
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" | grep -E "(gateway|job-navigator|mysql|redis)"

# Health 체크
curl -s "http://localhost:12085/actuator/health" | jq '.status'  # 직접
curl -s "http://localhost:8080/api/job-navigator/actuator/health" | jq '.status'  # Gateway 통해서
```

### 2. API 테스트 순서

#### Step 1: 기본 동작 확인
```bash
# 1. 목록 조회 (캐시 비운 후)
docker exec asyncsite-redis redis-cli FLUSHALL
curl -s "http://localhost:8080/api/job-navigator/jobs" | jq '.totalElements'

# 2. 개별 조회
curl -s "http://localhost:8080/api/job-navigator/jobs/1" | jq '.id, .title'
```

#### Step 2: 필터링 테스트
```bash
# 필터링 테스트 시 주의사항:
# 1. 먼저 전체 데이터 확인
curl -s "http://localhost:8080/api/job-navigator/jobs" | jq '.content[] | {id, title, skills}'

# 2. DB에서 실제 관계 확인
docker exec -i asyncsite-mysql mysql -u root -pasyncsite_root_2024! job_db -e "
SELECT jp.id, jp.title, GROUP_CONCAT(ts.name) as skills
FROM job_postings jp
LEFT JOIN job_tech_stacks jts ON jp.id = jts.job_posting_id
LEFT JOIN tech_stacks ts ON jts.tech_stack_id = ts.id
GROUP BY jp.id"

# 3. 필터링 테스트
curl -s "http://localhost:8080/api/job-navigator/jobs?techStackIds=1" | jq '{total: .totalElements, jobs: [.content[] | .title]}'
```

### 3. 로그 확인 방법

#### 실시간 로그 모니터링
```bash
# 로그를 보면서 동시에 요청 보내기
docker logs -f asyncsite-job-navigator --tail 0 &
LOG_PID=$!
sleep 2
curl -s "http://localhost:8080/api/job-navigator/jobs?techStackIds=1" > /dev/null
sleep 3
kill $LOG_PID 2>/dev/null
```

#### 디버깅을 위한 로그 레벨 변경
application.yml에서:
```yaml
logging:
  level:
    com.asyncsite.jobnavigator: DEBUG  # INFO → DEBUG로 변경
    com.asyncsite.jobnavigator.application.service: DEBUG
```

### 4. 성능 테스트
```bash
# 캐시 성능 측정
echo "=== Redis 캐싱 성능 테스트 ==="
echo "1. 캐시 비우기"
docker exec asyncsite-redis redis-cli FLUSHALL

echo "2. 첫 번째 호출 (캐시 미스)"
time curl -s "http://localhost:8080/api/job-navigator/jobs" > /dev/null

echo "3. 두 번째 호출 (캐시 히트)"
time curl -s "http://localhost:8080/api/job-navigator/jobs" > /dev/null
```

## 일반적인 문제와 해결법

### 1. "TechStack이 로드되지 않음" 오류
**증상**: API 응답의 skills 배열이 비어있음

**확인 방법**:
```bash
# 1. DB 데이터 확인
docker exec -i asyncsite-mysql mysql -u root -pasyncsite_root_2024! job_db -e "SELECT COUNT(*) FROM job_tech_stacks"

# 2. JPA 쿼리 로그 활성화
# application.yml에 추가:
# logging.level.org.hibernate.SQL: DEBUG
```

**주의**: 캐시 때문일 수 있으니 먼저 캐시를 비우고 테스트하세요!

### 2. 필터링이 작동하지 않음
**체크리스트**:
1. ✅ JobService에 필터링 로직이 구현되어 있는가?
2. ✅ Docker 이미지를 재빌드했는가?
3. ✅ 캐시를 비웠는가?
4. ✅ 로그에서 필터링 로직이 실행되는지 확인했는가?

### 3. Gateway 통합 문제
**Gateway 통한 호출이 안 될 때**:
```bash
# 1. Gateway 라우팅 확인
curl -s "http://localhost:8080/actuator/gateway/routes" | jq '.[] | select(.id == "job-navigator-service")'

# 2. 직접 서비스 호출 테스트
curl -s "http://localhost:12085/jobs"  # 직접
curl -s "http://localhost:8080/api/job-navigator/jobs"  # Gateway 통해서
```

## 테스트 자동화 스크립트

다음은 전체 테스트를 자동으로 수행하는 스크립트입니다:

```bash
#!/bin/bash
# test-job-navigator.sh

echo "=== Job Navigator Service 종합 테스트 ==="

# 1. 서비스 상태 확인
echo -e "\n1. 서비스 상태 확인"
docker ps | grep -E "(gateway|job-navigator|mysql|redis)" || { echo "필수 서비스가 실행되지 않음"; exit 1; }

# 2. 캐시 초기화
echo -e "\n2. Redis 캐시 초기화"
docker exec asyncsite-redis redis-cli FLUSHALL

# 3. API 테스트
echo -e "\n3. API 엔드포인트 테스트"
echo "- GET /jobs"
curl -s "http://localhost:8080/api/job-navigator/jobs" | jq '{total: .totalElements}' || echo "실패"

echo "- GET /jobs/1"
curl -s "http://localhost:8080/api-job-navigator/jobs/1" | jq '.id' || echo "실패"

echo "- GET /companies"
curl -s "http://localhost:8080/api/job-navigator/companies" | jq 'length' || echo "실패"

echo "- GET /tech-stacks"
curl -s "http://localhost:8080/api/job-navigator/tech-stacks" | jq 'length' || echo "실패"

# 4. 필터링 테스트
echo -e "\n4. 필터링 테스트"
echo "- 키워드 검색"
curl -s "http://localhost:8080/api/job-navigator/jobs?keyword=백엔드" | jq '.totalElements' || echo "실패"

echo "- TechStack 필터링"
curl -s "http://localhost:8080/api/job-navigator/jobs?techStackIds=1" | jq '.totalElements' || echo "실패"

# 5. 성능 테스트
echo -e "\n5. 캐싱 성능 테스트"
START=$(date +%s%3N)
curl -s "http://localhost:8080/api/job-navigator/jobs?page=0&size=20" > /dev/null
END=$(date +%s%3N)
echo "첫 번째 호출: $((END-START))ms"

START=$(date +%s%3N)
curl -s "http://localhost:8080/api/job-navigator/jobs?page=0&size=20" > /dev/null
END=$(date +%s%3N)
echo "두 번째 호출 (캐시): $((END-START))ms"

echo -e "\n=== 테스트 완료 ==="
```

## 핵심 교훈

1. **항상 Docker 이미지 재빌드**: 코드 변경 후 restart만 하지 말고 build를 꼭 하세요.
2. **캐시를 의심하라**: 이상한 결과가 나오면 먼저 Redis 캐시를 비우세요.
3. **로그 레벨 조정**: 디버깅할 때는 DEBUG 레벨로, 운영에서는 INFO 레벨로.
4. **DB 직접 확인**: JPA가 예상과 다르게 동작할 수 있으니 DB를 직접 확인하세요.
5. **Gateway vs 직접 호출**: 문제 격리를 위해 두 가지 방법 모두 테스트하세요.

## 다음 작업자를 위한 체크리스트

- [ ] Docker Desktop이 실행 중인가?
- [ ] 필수 서비스(MySQL, Redis, Eureka)가 모두 실행 중인가?
- [ ] 코드 변경 후 Docker 이미지를 재빌드했는가?
- [ ] 테스트 전 Redis 캐시를 비웠는가?
- [ ] 로그를 실시간으로 모니터링하고 있는가?
- [ ] DB에 테스트 데이터가 있는가?