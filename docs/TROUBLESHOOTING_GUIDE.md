# Job Navigator Service - 문제 해결 가이드

> ⚠️ **주의**: 이 문서의 내용은 다른 문서들에 통합되었습니다.

## 최신 정보는 다음 문서를 참조하세요:

### 1. **AI_BACKEND_CONTEXT.md** (메인 문서)
- 프로젝트 현재 상태
- 해결된 문제들과 시행착오
- 다음 AI가 해야 할 작업
- 유용한 명령어 모음

### 2. **TESTING_GUIDELINE.md**
- 테스트 방법론
- 일반적인 문제와 해결법
- 테스트 자동화 스크립트

### 3. **backend_design_v1.md**
- 섹션 8.2: 주요 기술적 이슈와 해결
- 섹션 8.5: 중요 개발 팁

### 4. **frontend-backend-integration-analysis.md**
- 섹션 9.2: 해결된 주요 이슈와 시행착오
- 섹션 9.5: 프론트엔드 통합 시 주의사항

## 빠른 참조

### 🚨 가장 중요한 것
```bash
# Docker 이미지 재빌드 (코드 변경 시 필수!)
./gradlew clean build -x test
docker-compose -f docker-compose.job-navigator-only.yml build job-navigator-service
docker-compose -f docker-compose.job-navigator-only.yml up -d job-navigator-service
```

### 자주 발생하는 문제
1. **코드 변경이 반영 안됨** → Docker 이미지 재빌드
2. **필터링이 안됨** → Redis 캐시 비우기 (`docker exec asyncsite-redis redis-cli FLUSHALL`)
3. **401 오류** → SecurityConfig 확인
4. **skills 배열 비어있음** → Fetch Join 쿼리 확인

---

**권장사항**: 개발 시작 전 `AI_BACKEND_CONTEXT.md`를 먼저 읽으세요.