# Job Crawler Service 구현 가이드

> 이 문서는 Job Crawler Service를 구현하는 다음 AI를 위한 상세 가이드입니다.
> Python 기반의 크롤러 서비스를 Clean Architecture로 구현합니다.

## 📋 선행 조건 확인

### 1. Job Navigator Service 상태
- ✅ 모든 API 엔드포인트 구현 완료
- ✅ 웹 프론트엔드와 완벽한 연동
- ✅ Mock 데이터로 전체 플로우 검증
- ✅ 배치 저장 API 준비 필요 (`POST /api/jobs/batch`)

### 2. 개발 환경
```bash
# Python 3.11+ 필요
python --version

# 가상환경 생성
python -m venv venv
source venv/bin/activate  # Windows: venv\Scripts\activate

# 프로젝트 디렉토리
cd /Users/Rene/Documents/rene/project/asyncsite
mkdir job-crawler-service
cd job-crawler-service
```

## 🏗️ 프로젝트 구조 생성

```bash
# 디렉토리 구조 생성
mkdir -p src/adapter/{inbound/{api,scheduler},outbound/{crawler,client}}
mkdir -p src/application/{port/{inbound,outbound},service,dto}
mkdir -p src/domain
mkdir -p src/infrastructure/{config,logging,monitoring}
mkdir -p tests/{unit,integration}

# __init__.py 파일 생성
find src -type d -exec touch {}/__init__.py \;
```

## 🔧 기본 설정 파일

### 1. requirements.txt
```txt
# Core
fastapi==0.109.2
uvicorn[standard]==0.27.0
python-dotenv==1.0.0

# HTTP Client
httpx==0.26.0

# Web Scraping
beautifulsoup4==4.12.3
lxml==5.1.0

# Scheduling
apscheduler==3.10.4

# Data Validation
pydantic==2.5.3
pydantic-settings==2.1.0

# Logging
structlog==24.1.0

# Monitoring
prometheus-client==0.19.0

# Testing
pytest==7.4.4
pytest-asyncio==0.23.3
pytest-cov==4.1.0
httpx-mock==0.27.0

# Development
black==23.12.1
ruff==0.1.11
mypy==1.8.0
```

### 2. .env.example
```env
# Service Configuration
SERVICE_NAME=job-crawler-service
SERVICE_PORT=8001
ENVIRONMENT=development

# Job Navigator Service
JOB_SERVICE_URL=http://localhost:12085
JOB_SERVICE_TIMEOUT=30

# Crawler Settings
CRAWLER_USER_AGENT=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36
CRAWLER_REQUEST_DELAY_MIN=1.0
CRAWLER_REQUEST_DELAY_MAX=2.0
CRAWLER_MAX_RETRIES=3
CRAWLER_BACKOFF_FACTOR=2.0

# Scheduler Settings
SCHEDULER_ENABLED=true
SCHEDULER_CRON_HOUR=3
SCHEDULER_CRON_MINUTE=0

# Logging
LOG_LEVEL=INFO
LOG_FORMAT=json

# Monitoring
METRICS_ENABLED=true
METRICS_PORT=9090
```

### 3. Dockerfile
```dockerfile
FROM python:3.11-slim

WORKDIR /app

# Install system dependencies
RUN apt-get update && apt-get install -y \
    gcc \
    && rm -rf /var/lib/apt/lists/*

# Copy requirements first for better caching
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copy application code
COPY src/ ./src/
COPY .env.example .env

# Create non-root user
RUN useradd -m -u 1000 crawler && chown -R crawler:crawler /app
USER crawler

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD python -c "import httpx; httpx.get('http://localhost:8001/health')"

EXPOSE 8001

CMD ["uvicorn", "src.main:app", "--host", "0.0.0.0", "--port", "8001"]
```

## 📝 핵심 구현 코드

### 1. 도메인 모델
```python
# src/domain/job.py
from dataclasses import dataclass
from datetime import datetime
from typing import List, Optional
from enum import Enum

class JobType(str, Enum):
    FULLTIME = "FULLTIME"
    CONTRACT = "CONTRACT"
    INTERN = "INTERN"
    PARTTIME = "PARTTIME"

class ExperienceLevel(str, Enum):
    JUNIOR = "JUNIOR"
    SENIOR = "SENIOR"
    LEAD = "LEAD"
    ANY = "ANY"

@dataclass
class Job:
    """채용공고 도메인 모델"""
    title: str
    company_name: str
    description: str
    requirements: str
    preferred: Optional[str]
    tech_stacks: List[str]
    job_type: JobType
    experience_level: ExperienceLevel
    location: str
    source_url: str
    posted_at: Optional[datetime]
    expires_at: Optional[datetime]
    
    def to_api_format(self) -> dict:
        """Job Navigator Service API 형식으로 변환"""
        return {
            "title": self.title,
            "companyName": self.company_name,
            "description": self.description,
            "requirements": self.requirements,
            "preferred": self.preferred,
            "techStackNames": self.tech_stacks,
            "jobType": self.job_type.value,
            "experienceLevel": self.experience_level.value,
            "location": self.location,
            "sourceUrl": self.source_url,
            "postedAt": self.posted_at.isoformat() if self.posted_at else None,
            "expiresAt": self.expires_at.isoformat() if self.expires_at else None
        }
```

### 2. 크롤러 베이스 클래스
```python
# src/adapter/outbound/crawler/base_crawler.py
import asyncio
import random
from abc import ABC, abstractmethod
from typing import List, Optional
import httpx
from bs4 import BeautifulSoup
from structlog import get_logger

from src.domain.job import Job
from src.infrastructure.config.settings import get_settings

logger = get_logger()
settings = get_settings()

class BaseCrawler(ABC):
    """크롤러 베이스 클래스"""
    
    def __init__(self, company_name: str):
        self.company_name = company_name
        self.client = self._create_client()
        
    def _create_client(self) -> httpx.AsyncClient:
        """HTTP 클라이언트 생성"""
        return httpx.AsyncClient(
            headers={
                'User-Agent': settings.CRAWLER_USER_AGENT,
                'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8',
                'Accept-Language': 'ko-KR,ko;q=0.9,en;q=0.8',
                'Accept-Encoding': 'gzip, deflate',
                'Connection': 'keep-alive',
                'Upgrade-Insecure-Requests': '1'
            },
            timeout=30.0,
            follow_redirects=True
        )
    
    @abstractmethod
    async def fetch_job_list(self) -> List[str]:
        """채용공고 URL 목록을 가져옵니다."""
        pass
    
    @abstractmethod
    async def parse_job_detail(self, url: str) -> Optional[Job]:
        """채용공고 상세 정보를 파싱합니다."""
        pass
    
    async def crawl(self, max_jobs: int = 50) -> List[Job]:
        """메인 크롤링 로직"""
        jobs = []
        
        try:
            # 1. 채용공고 URL 목록 수집
            job_urls = await self.fetch_job_list()
            logger.info(
                "fetched_job_urls",
                company=self.company_name,
                count=len(job_urls)
            )
            
            # 2. 각 URL에 대해 상세 정보 크롤링
            for i, url in enumerate(job_urls[:max_jobs]):
                try:
                    # Rate limiting
                    delay = random.uniform(
                        settings.CRAWLER_REQUEST_DELAY_MIN,
                        settings.CRAWLER_REQUEST_DELAY_MAX
                    )
                    await asyncio.sleep(delay)
                    
                    # 상세 정보 파싱
                    job = await self.parse_job_detail(url)
                    if job:
                        jobs.append(job)
                        logger.info(
                            "parsed_job",
                            company=self.company_name,
                            title=job.title,
                            progress=f"{i+1}/{len(job_urls)}"
                        )
                    
                except Exception as e:
                    logger.error(
                        "parse_job_failed",
                        company=self.company_name,
                        url=url,
                        error=str(e)
                    )
                    continue
                    
        except Exception as e:
            logger.error(
                "crawl_failed",
                company=self.company_name,
                error=str(e)
            )
            raise
        finally:
            await self.client.aclose()
            
        logger.info(
            "crawl_completed",
            company=self.company_name,
            jobs_found=len(jobs)
        )
        
        return jobs
    
    async def _fetch_page(self, url: str) -> str:
        """페이지 HTML을 가져옵니다."""
        response = await self.client.get(url)
        response.raise_for_status()
        return response.text
    
    def _parse_html(self, html: str) -> BeautifulSoup:
        """HTML을 파싱합니다."""
        return BeautifulSoup(html, 'lxml')
```

### 3. 네이버 크롤러 구현 예시
```python
# src/adapter/outbound/crawler/naver_crawler.py
from datetime import datetime
from typing import List, Optional
import re

from src.adapter.outbound.crawler.base_crawler import BaseCrawler
from src.domain.job import Job, JobType, ExperienceLevel

class NaverCrawler(BaseCrawler):
    """네이버 채용 크롤러"""
    
    BASE_URL = "https://recruit.navercorp.com"
    
    def __init__(self):
        super().__init__("네이버")
    
    async def fetch_job_list(self) -> List[str]:
        """네이버 채용공고 URL 목록 수집"""
        job_urls = []
        
        # 개발 직군 페이지
        list_url = f"{self.BASE_URL}/naver/job/list/developer"
        html = await self._fetch_page(list_url)
        soup = self._parse_html(html)
        
        # 채용공고 링크 추출 (실제 구조에 맞게 수정 필요)
        job_cards = soup.find_all("div", class_="card_job")
        for card in job_cards:
            link = card.find("a")
            if link and link.get("href"):
                job_url = f"{self.BASE_URL}{link['href']}"
                job_urls.append(job_url)
        
        return job_urls
    
    async def parse_job_detail(self, url: str) -> Optional[Job]:
        """네이버 채용공고 상세 정보 파싱"""
        try:
            html = await self._fetch_page(url)
            soup = self._parse_html(html)
            
            # 제목
            title = soup.find("h2", class_="job_title").get_text(strip=True)
            
            # 설명
            description_elem = soup.find("div", class_="job_description")
            description = description_elem.get_text(strip=True) if description_elem else ""
            
            # 자격요건
            requirements_elem = soup.find("div", class_="job_requirements")
            requirements = requirements_elem.get_text(strip=True) if requirements_elem else ""
            
            # 우대사항
            preferred_elem = soup.find("div", class_="job_preferred")
            preferred = preferred_elem.get_text(strip=True) if preferred_elem else None
            
            # 기술 스택 추출
            tech_stacks = self._extract_tech_stacks(description + " " + requirements)
            
            # 경력 수준 판단
            experience_level = self._determine_experience_level(title, requirements)
            
            # 근무지
            location_elem = soup.find("span", class_="job_location")
            location = location_elem.get_text(strip=True) if location_elem else "서울"
            
            return Job(
                title=title,
                company_name=self.company_name,
                description=description,
                requirements=requirements,
                preferred=preferred,
                tech_stacks=tech_stacks,
                job_type=JobType.FULLTIME,
                experience_level=experience_level,
                location=location,
                source_url=url,
                posted_at=datetime.now(),
                expires_at=None
            )
            
        except Exception as e:
            logger.error("parse_detail_failed", url=url, error=str(e))
            return None
    
    def _extract_tech_stacks(self, text: str) -> List[str]:
        """텍스트에서 기술 스택 추출"""
        # 일반적인 기술 스택 패턴
        tech_patterns = [
            r'\b(Java|Kotlin|Python|JavaScript|TypeScript|Go|Rust|C\+\+|C#)\b',
            r'\b(Spring|Django|FastAPI|React|Vue|Angular|Node\.js)\b',
            r'\b(MySQL|PostgreSQL|MongoDB|Redis|Elasticsearch)\b',
            r'\b(AWS|GCP|Azure|Docker|Kubernetes|Jenkins)\b',
            r'\b(Git|GitHub|GitLab|Jira|Slack)\b'
        ]
        
        tech_stacks = set()
        for pattern in tech_patterns:
            matches = re.findall(pattern, text, re.IGNORECASE)
            tech_stacks.update(matches)
        
        return list(tech_stacks)
    
    def _determine_experience_level(self, title: str, requirements: str) -> ExperienceLevel:
        """경력 수준 판단"""
        text = f"{title} {requirements}".lower()
        
        if any(word in text for word in ["신입", "junior", "주니어"]):
            return ExperienceLevel.JUNIOR
        elif any(word in text for word in ["시니어", "senior", "경력"]):
            return ExperienceLevel.SENIOR
        elif any(word in text for word in ["리드", "lead", "팀장"]):
            return ExperienceLevel.LEAD
        else:
            return ExperienceLevel.ANY
```

### 4. Job Service 클라이언트
```python
# src/adapter/outbound/client/job_service_client.py
import httpx
from typing import List, Dict
from structlog import get_logger

from src.domain.job import Job
from src.infrastructure.config.settings import get_settings

logger = get_logger()
settings = get_settings()

class JobServiceClient:
    """Job Navigator Service 클라이언트"""
    
    def __init__(self):
        self.base_url = settings.JOB_SERVICE_URL
        self.client = httpx.AsyncClient(
            timeout=settings.JOB_SERVICE_TIMEOUT
        )
    
    async def send_jobs(self, jobs: List[Job]) -> Dict:
        """크롤링한 채용공고를 Job Navigator Service로 전송"""
        try:
            # Job 객체를 API 형식으로 변환
            job_data = [job.to_api_format() for job in jobs]
            
            response = await self.client.post(
                f"{self.base_url}/api/jobs/batch",
                json=job_data
            )
            response.raise_for_status()
            
            result = response.json()
            logger.info(
                "jobs_sent",
                count=len(jobs),
                response=result
            )
            
            return result
            
        except Exception as e:
            logger.error(
                "send_jobs_failed",
                error=str(e),
                jobs_count=len(jobs)
            )
            raise
    
    async def close(self):
        """클라이언트 종료"""
        await self.client.aclose()
```

### 5. 크롤링 서비스
```python
# src/application/service/crawl_service.py
from typing import List, Dict
from structlog import get_logger

from src.adapter.outbound.crawler.naver_crawler import NaverCrawler
from src.adapter.outbound.crawler.kakao_crawler import KakaoCrawler
from src.adapter.outbound.crawler.woowahan_crawler import WoowahanCrawler
from src.adapter.outbound.client.job_service_client import JobServiceClient

logger = get_logger()

class CrawlService:
    """크롤링 서비스"""
    
    def __init__(self):
        self.crawlers = {
            "naver": NaverCrawler,
            "kakao": KakaoCrawler,
            "woowahan": WoowahanCrawler
        }
        self.job_client = JobServiceClient()
    
    async def crawl_company(self, company: str) -> Dict:
        """특정 회사 크롤링"""
        if company not in self.crawlers:
            raise ValueError(f"Unknown company: {company}")
        
        logger.info("crawl_started", company=company)
        
        try:
            # 크롤러 실행
            crawler = self.crawlers[company]()
            jobs = await crawler.crawl()
            
            # Job Service로 전송
            if jobs:
                result = await self.job_client.send_jobs(jobs)
            else:
                result = {"message": "No jobs found"}
            
            logger.info(
                "crawl_completed",
                company=company,
                jobs_found=len(jobs)
            )
            
            return {
                "company": company,
                "jobs_found": len(jobs),
                "result": result
            }
            
        except Exception as e:
            logger.error(
                "crawl_failed",
                company=company,
                error=str(e)
            )
            raise
    
    async def crawl_all_companies(self) -> List[Dict]:
        """모든 회사 크롤링"""
        results = []
        
        for company in self.crawlers.keys():
            try:
                result = await self.crawl_company(company)
                results.append(result)
            except Exception as e:
                logger.error(
                    "company_crawl_failed",
                    company=company,
                    error=str(e)
                )
                results.append({
                    "company": company,
                    "error": str(e)
                })
        
        return results
```

### 6. FastAPI 메인 애플리케이션
```python
# src/main.py
from contextlib import asynccontextmanager
from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from prometheus_client import make_asgi_app
import structlog

from src.adapter.inbound.api.crawler_controller import router as crawler_router
from src.adapter.inbound.scheduler.crawl_scheduler import CrawlScheduler
from src.application.service.crawl_service import CrawlService
from src.infrastructure.config.settings import get_settings

logger = structlog.get_logger()
settings = get_settings()

# 전역 객체
crawl_service = CrawlService()
scheduler = None

@asynccontextmanager
async def lifespan(app: FastAPI):
    """애플리케이션 생명주기 관리"""
    global scheduler
    
    # 시작
    logger.info("Starting Job Crawler Service")
    
    # 스케줄러 시작
    if settings.SCHEDULER_ENABLED:
        scheduler = CrawlScheduler(crawl_service)
        scheduler.start()
        logger.info("Scheduler started")
    
    yield
    
    # 종료
    logger.info("Shutting down Job Crawler Service")
    if scheduler:
        scheduler.stop()
    await crawl_service.job_client.close()

# FastAPI 앱 생성
app = FastAPI(
    title="Job Crawler Service",
    version="1.0.0",
    lifespan=lifespan
)

# CORS 설정
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# 라우터 등록
app.include_router(crawler_router, prefix="/api/crawler", tags=["crawler"])

# Prometheus 메트릭 엔드포인트
if settings.METRICS_ENABLED:
    metrics_app = make_asgi_app()
    app.mount("/metrics", metrics_app)

# 헬스체크
@app.get("/health")
async def health_check():
    return {"status": "healthy"}

# 루트 엔드포인트
@app.get("/")
async def root():
    return {
        "service": "Job Crawler Service",
        "version": "1.0.0",
        "docs": "/docs"
    }
```

## 🧪 테스트 전략

### 1. 단위 테스트
```python
# tests/unit/test_tech_stack_extractor.py
import pytest
from src.adapter.outbound.crawler.naver_crawler import NaverCrawler

class TestTechStackExtractor:
    def test_extract_tech_stacks(self):
        crawler = NaverCrawler()
        text = "Java Spring Boot 개발자 모집. React, MySQL 경험 필수"
        
        tech_stacks = crawler._extract_tech_stacks(text)
        
        assert "Java" in tech_stacks
        assert "Spring" in tech_stacks
        assert "React" in tech_stacks
        assert "MySQL" in tech_stacks
```

### 2. 통합 테스트
```python
# tests/integration/test_crawl_service.py
import pytest
from httpx_mock import HTTPXMock

from src.application.service.crawl_service import CrawlService

@pytest.mark.asyncio
async def test_crawl_company_success(httpx_mock: HTTPXMock):
    # Mock 설정
    httpx_mock.add_response(
        url="http://localhost:12085/api/jobs/batch",
        json={"created": 5, "updated": 2}
    )
    
    # 테스트 실행
    service = CrawlService()
    result = await service.crawl_company("naver")
    
    assert result["company"] == "naver"
    assert "jobs_found" in result
```

## 🚀 실행 및 배포

### 1. 로컬 실행
```bash
# 환경변수 설정
cp .env.example .env

# 가상환경 활성화
source venv/bin/activate

# 의존성 설치
pip install -r requirements.txt

# 개발 서버 실행
uvicorn src.main:app --reload --port 8001
```

### 2. Docker 실행
```bash
# 이미지 빌드
docker build -t job-crawler-service .

# 컨테이너 실행
docker run -d \
  --name job-crawler \
  -p 8001:8001 \
  -e JOB_SERVICE_URL=http://host.docker.internal:12085 \
  job-crawler-service
```

### 3. docker-compose 통합
```yaml
# asyncsite/docker-compose.yml에 추가
job-crawler-service:
  build: ./job-crawler-service
  container_name: asyncsite-job-crawler
  ports:
    - "8001:8001"
  environment:
    - JOB_SERVICE_URL=http://job-navigator-service:12085
    - ENVIRONMENT=production
  depends_on:
    - job-navigator-service
  networks:
    - asyncsite-network
```

## 📊 모니터링

### 1. 로그 확인
```bash
# Docker 로그
docker logs -f job-crawler

# 구조화된 로그 검색
docker logs job-crawler | jq 'select(.company == "naver")'
```

### 2. 메트릭 확인
```bash
# Prometheus 메트릭
curl http://localhost:8001/metrics

# 크롤링 통계
curl http://localhost:8001/api/crawler/stats
```

## ⚠️ 주의사항

### 1. 크롤링 윤리
- robots.txt 확인 및 준수
- User-Agent 명시
- 요청 간 딜레이 (1-2초)
- 과도한 요청 금지

### 2. 에러 처리
- 네트워크 오류: 지수 백오프로 재시도
- 파싱 오류: 로그 남기고 건너뛰기
- 구조 변경: 알림 발송

### 3. 데이터 검증
- 필수 필드 확인
- 중복 제거 (source_url)
- 이상 데이터 필터링

## 🎯 다음 단계

1. **추가 크롤러 구현**
   - 카카오 크롤러
   - 쿠팡 크롤러
   - 배달의민족 크롤러

2. **고도화**
   - 병렬 크롤링
   - 프록시 로테이션
   - 캡차 대응

3. **운영 준비**
   - 알림 시스템
   - 대시보드
   - 백업 전략

---

**작성일**: 2025-08-01  
**문의**: AI_BACKEND_CONTEXT.md 참조