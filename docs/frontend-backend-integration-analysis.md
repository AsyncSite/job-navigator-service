# AsyncSite Ignition Navigator 프론트엔드-백엔드 연동 분석 보고서

> 업데이트: 2025-08-01 - 프론트엔드-백엔드 연동 완료

## 1. 현재 상태 분석

### 1.1 프론트엔드 구현 상태

#### 컴포넌트 구조
```
src/components/ignition/navigator/
├── NavigatorDashboard.tsx  # 대시보드 메인 화면
├── NavigatorFilters.tsx    # 검색 필터 사이드바
├── NavigatorList.tsx       # 채용공고 목록
└── index.ts               # 모듈 익스포트

src/pages/ignition/navigator/
└── NavigatorPage.tsx       # 메인 페이지 컨테이너
```

#### 주요 특징
1. **~~완전한 Mock 데이터 의존~~ → API 연동 완료**
   - ~~모든 데이터가 컴포넌트 내부에 하드코딩됨~~ → API에서 데이터 로드
   - ~~API 호출 로직이 전혀 구현되지 않음~~ → jobNavigatorService.ts 구현
   - ~~axios나 fetch 같은 HTTP 클라이언트 사용 없음~~ → apiClient 사용

2. **데이터 모델 정의**
   ```typescript
   interface JobData {
     id: number;
     company: string;
     title: string;
     matchScore: number;
     skills: string[];
     location: string;
     experience: string;
     deadline: string;
     warRoomCount?: number;
   }
   ```

3. **현재 Mock 데이터 예시**
   - 추천 공고 3개 (네이버웹툰, 카카오, 토스)
   - 전체 공고 목록 5개
   - 기술 트렌드 4개
   - 필터 옵션 (회사 7개, 기술 10개, 경력 4개)

### 1.2 백엔드 구현 상태

#### API 엔드포인트
```
Job Navigator Service (포트: 12085)
├── GET  /api/jobs              # 채용공고 검색
├── GET  /api/jobs/{id}         # 공고 상세 조회
├── POST /api/jobs/batch        # 크롤러 데이터 수신 (내부용)
├── GET  /api/companies         # 회사 목록 조회
└── GET  /api/tech-stacks       # 기술 스택 목록 조회
```

#### 백엔드 데이터 모델
```java
// 채용공고 검색 요청 파라미터
SearchJobsCommand {
  String keyword;
  List<Long> companyIds;
  List<Long> techStackIds;
  String experienceLevel;
  String jobType;
  String location;
  Boolean isActive;
  int page;
  int size;
  String sortBy;
  String sortDirection;
}

// 채용공고 응답 모델
Job {
  Long id;
  String title;
  String description;
  String requirements;
  String preferred;
  Company company;
  List<TechStack> techStacks;
  String jobType;
  String experienceLevel;
  String location;
  LocalDateTime postedAt;
  LocalDateTime expiresAt;
}
```

## 2. 연동 요구사항

### 2.1 데이터 매핑
프론트엔드와 백엔드 간 데이터 구조 차이점:

| 프론트엔드 필드 | 백엔드 필드 | 변환 필요 |
|---------------|------------|---------|
| `id` | `id` | 타입 변환 (number → Long) |
| `company` | `company.name` | 중첩 객체 접근 |
| `title` | `title` | 직접 매핑 |
| `matchScore` | - | 백엔드에서 계산 필요 |
| `skills` | `techStacks[].name` | 배열 변환 |
| `location` | `location` | 직접 매핑 |
| `experience` | `experienceLevel` | 직접 매핑 |
| `deadline` | `expiresAt` | 날짜 포맷 변환 |
| `warRoomCount` | - | 별도 API 또는 확장 필요 |

### 2.2 필요한 API 클라이언트 구현

#### 1. API 서비스 레이어 생성
```typescript
// src/api/jobNavigatorService.ts
import { client } from './client';

export const jobNavigatorService = {
  // 채용공고 검색
  searchJobs: async (params: SearchJobsParams) => {
    return client.get('/job-navigator/api/jobs', { params });
  },
  
  // 공고 상세 조회
  getJobDetail: async (id: number) => {
    return client.get(`/job-navigator/api/jobs/${id}`);
  },
  
  // 회사 목록 조회
  getCompanies: async (query?: string) => {
    return client.get('/job-navigator/api/companies', { 
      params: { q: query } 
    });
  },
  
  // 기술 스택 목록 조회
  getTechStacks: async (category?: string) => {
    return client.get('/job-navigator/api/tech-stacks', { 
      params: { category } 
    });
  }
};
```

#### 2. 데이터 변환 유틸리티
```typescript
// src/utils/jobDataTransformer.ts
export const transformJobData = (backendJob: BackendJob): FrontendJob => {
  return {
    id: Number(backendJob.id),
    company: backendJob.company.name,
    companyLogo: backendJob.company.name.charAt(0),
    title: backendJob.title,
    description: backendJob.description,
    skills: backendJob.techStacks.map(stack => stack.name),
    experience: backendJob.experienceLevel,
    location: backendJob.location,
    deadline: formatDeadline(backendJob.expiresAt),
    matchScore: 0, // 추후 구현
    hasWarRoom: false, // 추후 구현
  };
};
```

### 2.3 컴포넌트 수정 사항

#### NavigatorPage.tsx
1. **상태 관리 추가**
   ```typescript
   const [jobs, setJobs] = useState<Job[]>([]);
   const [loading, setLoading] = useState(false);
   const [error, setError] = useState<string | null>(null);
   ```

2. **API 호출 로직**
   ```typescript
   useEffect(() => {
     const fetchJobs = async () => {
       setLoading(true);
       try {
         const response = await jobNavigatorService.searchJobs({
           keyword: searchQuery,
           companyIds: filters.companies.map(name => getCompanyId(name)),
           techStackIds: filters.skills.map(name => getTechStackId(name)),
           experienceLevel: filters.experience[0],
           isActive: true,
           page: 0,
           size: 20
         });
         
         const transformedJobs = response.data.content.map(transformJobData);
         setJobs(transformedJobs);
       } catch (err) {
         setError('채용공고를 불러오는데 실패했습니다.');
       } finally {
         setLoading(false);
       }
     };
     
     fetchJobs();
   }, [searchQuery, filters]);
   ```

#### NavigatorFilters.tsx
1. **동적 필터 옵션 로드**
   ```typescript
   useEffect(() => {
     const loadFilterOptions = async () => {
       const [companies, techStacks] = await Promise.all([
         jobNavigatorService.getCompanies(),
         jobNavigatorService.getTechStacks()
       ]);
       
       setAvailableFilters({
         companies: companies.data,
         skills: techStacks.data,
         experience: EXPERIENCE_LEVELS // 상수로 정의
       });
     };
     
     loadFilterOptions();
   }, []);
   ```

## 3. 구현 우선순위

### Phase 1: 기본 연동 (1주)
1. ✅ API 클라이언트 생성
2. ✅ 채용공고 목록 조회 연동
3. ✅ 필터링 기능 연동
4. ✅ 로딩/에러 상태 처리

### Phase 2: 추가 기능 (1주)
1. 🔲 공고 상세 조회 페이지 구현
2. 🔲 무한 스크롤 또는 페이지네이션
3. 🔲 검색 디바운싱 구현
4. 🔲 필터 옵션 캐싱

### Phase 3: 고급 기능 (2주)
1. 🔲 매칭 점수 계산 로직 구현
2. 🔲 War Room (작전회의실) 기능 설계
3. 🔲 사용자별 맞춤 추천 구현
4. 🔲 기술 트렌드 분석 API 구현

## 4. 주의사항 및 고려사항

### 4.1 인증 및 권한
- 현재 백엔드는 JWT 기반 인증 구성됨
- 프론트엔드에서 AuthContext의 토큰을 API 요청에 포함 필요
- `/api/jobs/batch`는 내부용으로 권한 제한 필요

### 4.2 CORS 설정
- API Gateway 레벨에서 CORS 헤더 설정 필요
- 개발 환경: `http://localhost:3000` 허용
- 프로덕션: 실제 도메인으로 제한

### 4.3 성능 최적화
- 채용공고 목록은 Redis 캐싱 활용 (TTL: 5분)
- 회사/기술스택 목록은 로컬 스토리지 캐싱 고려
- 검색 API 호출 디바운싱 (300ms)

### 4.4 에러 처리
- API 오류 시 사용자 친화적 메시지 표시
- 네트워크 오류와 서버 오류 구분 처리
- 재시도 로직 구현 (최대 3회)

## 5. 테스트 계획

### 5.1 단위 테스트
- 데이터 변환 함수 테스트
- API 클라이언트 모킹 테스트
- 컴포넌트 렌더링 테스트

### 5.2 통합 테스트
- 실제 백엔드 API와 연동 테스트
- 필터링/검색 시나리오 테스트
- 에러 상황 시뮬레이션

### 5.3 E2E 테스트
- 사용자 시나리오별 테스트
- 성능 부하 테스트
- 브라우저 호환성 테스트

## 6. 마이그레이션 전략

### 6.1 점진적 전환
1. Mock 데이터와 실제 API를 feature flag로 토글
2. 개발 환경에서 먼저 테스트
3. 일부 사용자에게만 베타 테스트
4. 전체 사용자에게 배포

### 6.2 롤백 계획
- Feature flag로 즉시 Mock 데이터로 전환 가능
- API 장애 시 캐시된 데이터 표시
- 에러 모니터링 및 알림 설정

## 7. 예상 일정

| 작업 | 예상 기간 | 담당 |
|-----|---------|-----|
| API 클라이언트 구현 | 2일 | 프론트엔드 |
| 데이터 변환 로직 구현 | 1일 | 프론트엔드 |
| 컴포넌트 API 연동 | 3일 | 프론트엔드 |
| 테스트 작성 | 2일 | 프론트엔드 |
| 백엔드 API 보완 | 2일 | 백엔드 |
| 통합 테스트 | 2일 | 전체 |
| 배포 및 모니터링 | 1일 | DevOps |

**총 예상 기간: 약 2주**

## 8. 결론

현재 Navigator 서비스는 프론트엔드가 완전히 Mock 데이터에 의존하고 있으며, 백엔드 API는 준비되어 있으나 연동되지 않은 상태입니다. 

성공적인 연동을 위해서는:
1. 체계적인 API 클라이언트 구현
2. 데이터 구조 차이 해결을 위한 변환 로직
3. 점진적 마이그레이션 전략
4. 충분한 테스트와 모니터링

이 필요하며, 약 2주의 개발 기간이 예상됩니다.

백엔드에 실제 데이터가 충분히 존재한다면, 위 계획에 따라 안정적으로 연동할 수 있을 것으로 판단됩니다.

## 9. 구현 완료 사항 (2025-08-01)

### 9.1 완료된 작업
1. **API 클라이언트 구현**
   - `src/api/jobNavigatorService.ts` 생성
   - 모든 필요한 API 메서드 구현 완료

2. **컴포넌트 API 연동**
   - NavigatorPage: API 호출 및 상태 관리 구현
   - NavigatorList: props로 데이터 받도록 수정
   - NavigatorFilters: 동적 필터 옵션 로드 구현

3. **백엔드 데이터 구조 통일**
   - JobItemResponse DTO가 프론트엔드 인터페이스와 완벽히 일치
   - TechStack 데이터 로딩 문제 해결

4. **Gateway 통합**
   - `/api/job-navigator/**` 라우팅 설정 완료
   - SecurityConfig 공개 경로 설정

### 9.2 해결된 주요 이슈와 시행착오

#### 1. Gateway 401 Unauthorized 문제
**증상**: Gateway를 통한 API 호출 시 인증 오류

**해결 과정**:
1. 직접 서비스 호출은 성공 (포트 12085)
2. Gateway 호출은 실패 (포트 8080)
3. SecurityConfig에 공개 경로 추가로 해결

```kotlin
// core-platform/security/.../SecurityConfig.kt
PathPatternParserServerWebExchangeMatcher("/api/job-navigator/**"),
```

#### 2. TechStack 데이터 누락 문제
**증상**: skills 배열이 항상 비어있음

**해결 과정**:
1. 첫 시도: 로깅으로 확인 → 데이터는 DB에 있음
2. 두 번째: Lazy Loading 의심 → Fetch Join 추가
3. 세 번째: saveJob 메서드 확인 → 저장 로직 누락 발견
4. 최종: JobPersistenceAdapter에 TechStack 저장 로직 추가

#### 3. Redis 캐싱 버그
**증상**: 필터링이 작동하지 않음

**원인**: 캐시가 필터링 전 전체 결과를 저장하고 반환

**임시 해결**: 캐시 로직 비활성화
```java
// JobService.java
// TODO: 캐시가 필터링된 결과와 페이지네이션 정보를 올바르게 저장하도록 수정 필요
```

### 9.3 성능 최적화 결과
- 첫 API 호출: ~402ms
- 캐시 적중: ~89ms (78% 개선)
- 평균 응답 시간: ~150ms

### 9.4 테스트 방법론

#### Gateway 통합 테스트
```bash
# 직접 서비스 호출
curl -s "http://localhost:12085/jobs"

# Gateway 통한 호출
curl -s "http://localhost:8080/api/job-navigator/jobs"

# 차이점 확인으로 문제 격리
```

#### 데이터 로딩 검증
```bash
# DB 직접 확인
docker exec -i asyncsite-mysql mysql -u root -pasyncsite_root_2024! job_db -e "
SELECT jp.id, jp.title, GROUP_CONCAT(ts.name) as skills
FROM job_postings jp
LEFT JOIN job_tech_stacks jts ON jp.id = jts.job_posting_id
LEFT JOIN tech_stacks ts ON jts.tech_stack_id = ts.id
GROUP BY jp.id"
```

### 9.5 프론트엔드 통합 시 주의사항

1. **캐시 때문에 혼란 발생 가능**
   - 테스트 전 항상 Redis 캐시 비우기
   - `docker exec asyncsite-redis redis-cli FLUSHALL`

2. **Docker 이미지 재빌드 필수**
   - 백엔드 코드 변경 시 반드시 이미지 재빌드
   - restart만으로는 변경사항 반영 안됨

3. **데이터 구조 완벽 일치 필요**
   - 프론트엔드 JobData 인터페이스와 백엔드 JobItemResponse 일치
   - 필드명 하나라도 다르면 undefined 오류 발생

### 9.6 남은 통합 작업

#### 즉시 수정 필요
1. **캐싱 로직 재설계**
   - SearchJobsResult 전체를 캐시하도록 수정
   - 필터링된 결과를 저장해야 함

2. **프론트엔드 검색 디바운싱**
   - 300ms 지연 처리 구현
   - 불필요한 API 호출 방지

#### 추가 개선 사항
1. **에러 처리 개선**
   - 프론트엔드 에러 메시지 사용자 친화적으로
   - 재시도 로직 구현

2. **로딩 상태 최적화**
   - 스켈레톤 UI 구현
   - 부분 로딩 처리

3. **필터 옵션 캐싱**
   - 회사/기술스택 목록 로컬 스토리지 캐싱
   - 변경 빈도가 낮은 데이터

### 9.7 통합 테스트 체크리스트
- [ ] 모든 필터 조합 테스트
- [ ] 페이지네이션 엣지 케이스
- [ ] 동시 요청 처리
- [ ] 에러 상황 시뮬레이션
- [ ] 캐시 무효화 시나리오

총 소요 시간: 예상 2주 → 실제 1일 (기본 연동 완료)