package com.asyncsite.jobnavigator.config;

import com.asyncsite.jobnavigator.application.port.out.LoadCompanyPort;
import com.asyncsite.jobnavigator.application.port.out.LoadJobPort;
import com.asyncsite.jobnavigator.application.port.out.LoadTechStackPort;
import com.asyncsite.jobnavigator.application.port.out.SaveCompanyPort;
import com.asyncsite.jobnavigator.application.port.out.SaveJobPort;
import com.asyncsite.jobnavigator.application.port.out.SaveTechStackPort;
import com.asyncsite.jobnavigator.domain.Company;
import com.asyncsite.jobnavigator.domain.Job;
import com.asyncsite.jobnavigator.domain.TechStack;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 개발 환경에서 초기 데이터를 생성하는 초기화 클래스
 * 프론트엔드 목업 데이터와 동일한 데이터를 생성
 */
@Component
@Profile({"local", "docker"})
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {
    
    private final SaveCompanyPort saveCompanyPort;
    private final SaveTechStackPort saveTechStackPort;
    private final SaveJobPort saveJobPort;
    private final LoadJobPort loadJobPort;
    
    @Override
    public void run(ApplicationArguments args) {
        log.info("초기 데이터 생성 시작...");
        
        try {
            // 이미 데이터가 있는지 확인
            if (loadJobPort.countActiveJobs() > 0) {
                log.info("이미 데이터가 존재합니다. 초기화를 건너뜁니다.");
                return;
            }
            
            // 1. 기술 스택 생성
            Map<String, TechStack> techStacks = createTechStacks();
            
            // 2. 회사 생성
            Map<String, Company> companies = createCompanies();
            
            // 3. 채용공고 생성
            createJobs(companies, techStacks);
            
            log.info("초기 데이터 생성 완료!");
            
        } catch (Exception e) {
            log.error("초기 데이터 생성 중 오류 발생", e);
        }
    }
    
    private Map<String, TechStack> createTechStacks() {
        Map<String, TechStack> techStackMap = new HashMap<>();
        
        // 언어
        techStackMap.put("Java", createAndSaveTechStack("Java", TechStack.Category.LANGUAGE));
        techStackMap.put("Kotlin", createAndSaveTechStack("Kotlin", TechStack.Category.LANGUAGE));
        techStackMap.put("Python", createAndSaveTechStack("Python", TechStack.Category.LANGUAGE));
        techStackMap.put("Go", createAndSaveTechStack("Go", TechStack.Category.LANGUAGE));
        techStackMap.put("TypeScript", createAndSaveTechStack("TypeScript", TechStack.Category.LANGUAGE));
        
        // 프레임워크
        techStackMap.put("Spring", createAndSaveTechStack("Spring", TechStack.Category.FRAMEWORK));
        techStackMap.put("Spring Boot", createAndSaveTechStack("Spring Boot", TechStack.Category.FRAMEWORK));
        techStackMap.put("React", createAndSaveTechStack("React", TechStack.Category.FRAMEWORK));
        techStackMap.put("Node.js", createAndSaveTechStack("Node.js", TechStack.Category.FRAMEWORK));
        
        // 데이터베이스
        techStackMap.put("MySQL", createAndSaveTechStack("MySQL", TechStack.Category.DATABASE));
        techStackMap.put("PostgreSQL", createAndSaveTechStack("PostgreSQL", TechStack.Category.DATABASE));
        techStackMap.put("Redis", createAndSaveTechStack("Redis", TechStack.Category.DATABASE));
        
        // 클라우드/인프라
        techStackMap.put("AWS", createAndSaveTechStack("AWS", TechStack.Category.CLOUD));
        techStackMap.put("Docker", createAndSaveTechStack("Docker", TechStack.Category.TOOL));
        techStackMap.put("Kubernetes", createAndSaveTechStack("Kubernetes", TechStack.Category.TOOL));
        techStackMap.put("k8s", createAndSaveTechStack("k8s", TechStack.Category.TOOL));
        
        // 메시지 큐/스트리밍
        techStackMap.put("Kafka", createAndSaveTechStack("Kafka", TechStack.Category.TOOL));
        techStackMap.put("MSA", createAndSaveTechStack("MSA", TechStack.Category.OTHER));
        techStackMap.put("gRPC", createAndSaveTechStack("gRPC", TechStack.Category.OTHER));
        
        log.info("기술 스택 {} 개 생성 완료", techStackMap.size());
        return techStackMap;
    }
    
    private TechStack createAndSaveTechStack(String name, TechStack.Category category) {
        TechStack techStack = TechStack.create(name, category);
        return saveTechStackPort.saveTechStack(techStack);
    }
    
    private Map<String, Company> createCompanies() {
        Map<String, Company> companyMap = new HashMap<>();
        
        companyMap.put("네이버웹툰", createAndSaveCompany("네이버웹툰", "NAVER WEBTOON", "https://webtoonscorp.com"));
        companyMap.put("카카오", createAndSaveCompany("카카오", "Kakao", "https://www.kakaocorp.com"));
        companyMap.put("쿠팡", createAndSaveCompany("쿠팡", "Coupang", "https://www.coupang.com"));
        companyMap.put("배달의민족", createAndSaveCompany("배달의민족", "Baedal Minjok", "https://www.baemin.com"));
        companyMap.put("토스", createAndSaveCompany("토스", "Toss", "https://toss.im"));
        companyMap.put("당근마켓", createAndSaveCompany("당근마켓", "Daangn Market", "https://www.daangn.com"));
        companyMap.put("라인", createAndSaveCompany("라인", "LINE", "https://linecorp.com"));
        companyMap.put("네이버", createAndSaveCompany("네이버", "NAVER", "https://www.navercorp.com"));
        companyMap.put("카카오뱅크", createAndSaveCompany("카카오뱅크", "Kakao Bank", "https://www.kakaobank.com"));
        companyMap.put("뱅크샐러드", createAndSaveCompany("뱅크샐러드", "Banksalad", "https://www.banksalad.com"));
        companyMap.put("야놀자", createAndSaveCompany("야놀자", "Yanolja", "https://www.yanolja.com"));
        companyMap.put("직방", createAndSaveCompany("직방", "Zigbang", "https://www.zigbang.com"));
        companyMap.put("무신사", createAndSaveCompany("무신사", "Musinsa", "https://www.musinsa.com"));
        companyMap.put("마켓컬리", createAndSaveCompany("마켓컬리", "Market Kurly", "https://www.kurly.com"));
        companyMap.put("오늘의집", createAndSaveCompany("오늘의집", "Bucketplace", "https://www.bucketplace.co.kr"));
        
        log.info("회사 {} 개 생성 완료", companyMap.size());
        return companyMap;
    }
    
    private Company createAndSaveCompany(String name, String nameEn, String careerPageUrl) {
        Company company = Company.create(name, nameEn, careerPageUrl, null);
        return saveCompanyPort.saveCompany(company);
    }
    
    private void createJobs(Map<String, Company> companies, Map<String, TechStack> techStacks) {
        // 1. 네이버웹툰 - 백엔드 서버 개발자
        createJob(
            companies.get("네이버웹툰"),
            "백엔드 서버 개발자 (네이버웹툰)",
            "네이버웹툰의 글로벌 서비스를 함께 만들어갈 백엔드 개발자를 찾습니다. 대용량 트래픽 처리와 안정적인 서비스 운영에 관심이 있으신 분을 환영합니다.",
            "• 5년 이상의 백엔드 개발 경험\n• Java, Spring Framework 개발 경험\n• 대용량 트래픽 처리 경험\n• RESTful API 설계 및 개발 경험\n• Linux 환경에서의 개발 및 운영 경험",
            "• Kotlin 개발 경험\n• MSA 아키텍처 설계 및 구축 경험\n• Kafka, Redis 등 미들웨어 사용 경험\n• 글로벌 서비스 개발 경험\n• 오픈소스 기여 경험",
            "분당",
            Job.JobType.FULLTIME,
            Job.ExperienceLevel.SENIOR,
            "https://recruit.webtoonscorp.com/job/1",
            LocalDateTime.now().plusDays(60),
            Arrays.asList(techStacks.get("Java"), techStacks.get("Spring Boot"), techStacks.get("Kotlin")),
            Arrays.asList(techStacks.get("MSA"), techStacks.get("Kafka"))
        );
        
        // 2. 카카오 - 카카오페이 결제 서버 개발자
        createJob(
            companies.get("카카오"),
            "카카오페이 결제 서버 개발자",
            "카카오페이 결제 시스템의 안정성과 확장성을 책임질 서버 개발자를 모십니다. 금융 서비스 개발 경험이 있으신 분을 우대합니다.",
            "• 5년 이상의 서버 개발 경험\n• Java, Spring Boot 숙련도\n• RDBMS 및 NoSQL 경험\n• 대용량 트랜잭션 처리 경험\n• 금융 도메인 이해",
            "• 결제 시스템 개발 경험\n• 분산 시스템 설계 경험\n• Kafka를 이용한 이벤트 기반 아키텍처 경험\n• 보안 및 암호화 지식\n• DevOps 경험",
            "판교",
            Job.JobType.FULLTIME,
            Job.ExperienceLevel.SENIOR,
            "https://careers.kakao.com/job/2",
            LocalDateTime.now().plusDays(75),
            Arrays.asList(techStacks.get("Java"), techStacks.get("Spring Boot"), techStacks.get("MySQL"), techStacks.get("Redis")),
            Arrays.asList(techStacks.get("Kafka"))
        );
        
        // 3. 쿠팡 - 물류 플랫폼 백엔드 개발자
        createJob(
            companies.get("쿠팡"),
            "물류 플랫폼 백엔드 개발자",
            "쿠팡의 혁신적인 물류 시스템을 개발하고 운영할 백엔드 개발자를 찾습니다. 대규모 분산 시스템 개발에 열정이 있는 분을 환영합니다.",
            "• 백엔드 개발 경험 (경력 무관)\n• Go 또는 Python 개발 능력\n• 클라우드 환경(AWS) 이해\n• 컨테이너 기술(Docker) 이해\n• 문제 해결 능력 및 학습 의지",
            "• 대규모 분산 시스템 개발 경험\n• Kubernetes 운영 경험\n• 실시간 데이터 처리 경험\n• 물류/이커머스 도메인 경험\n• 오픈소스 기여 경험",
            "송파",
            Job.JobType.FULLTIME,
            Job.ExperienceLevel.ANY,
            "https://www.coupang.jobs/job/3",
            LocalDateTime.now().plusDays(90),
            Arrays.asList(techStacks.get("Go"), techStacks.get("Python"), techStacks.get("AWS")),
            Arrays.asList(techStacks.get("Docker"), techStacks.get("Kubernetes"))
        );
        
        // 4. 배달의민족 - 딜리버리 서비스 백엔드
        createJob(
            companies.get("배달의민족"),
            "딜리버리 서비스 백엔드",
            "배달의민족 서비스의 핵심인 딜리버리 시스템을 개발할 백엔드 개발자를 모집합니다. 실시간 데이터 처리에 관심이 있으신 분을 찾습니다.",
            "• 2년 이상의 백엔드 개발 경험\n• Kotlin 또는 Java 개발 경험\n• Spring Framework 사용 경험\n• 실시간 시스템 개발에 대한 이해\n• 협업과 커뮤니케이션 능력",
            "• 위치 기반 서비스 개발 경험\n• Redis를 활용한 캐싱 경험\n• Kubernetes 환경 경험\n• gRPC 사용 경험\n• 모니터링 시스템 구축 경험",
            "송파",
            Job.JobType.FULLTIME,
            Job.ExperienceLevel.JUNIOR,
            "https://career.baemin.com/job/4",
            LocalDateTime.now().plusDays(55),
            Arrays.asList(techStacks.get("Kotlin"), techStacks.get("Spring"), techStacks.get("Redis")),
            Arrays.asList(techStacks.get("k8s"), techStacks.get("gRPC"))
        );
        
        // 5. 토스 - 금융 서비스 백엔드 개발자
        createJob(
            companies.get("토스"),
            "금융 서비스 백엔드 개발자",
            "토스의 혁신적인 금융 서비스를 만들어갈 백엔드 개발자를 찾습니다. 대규모 금융 시스템 개발 경험이 있으신 분을 우대합니다.",
            "• 4년 이상의 백엔드 개발 경험\n• Java, Spring Boot 전문성\n• RDBMS 설계 및 최적화 경험\n• 금융 서비스에 대한 관심\n• 안정적인 시스템 구축 능력",
            "• 금융권 시스템 개발 경험\n• 대용량 트랜잭션 처리 경험\n• Kafka 기반 이벤트 처리 경험\n• AWS 인프라 구축 경험\n• 보안 및 컴플라이언스 이해",
            "강남",
            Job.JobType.FULLTIME,
            Job.ExperienceLevel.SENIOR,
            "https://toss.im/career/job/5",
            LocalDateTime.now().plusDays(80),
            Arrays.asList(techStacks.get("Java"), techStacks.get("Spring Boot"), techStacks.get("PostgreSQL")),
            Arrays.asList(techStacks.get("Kafka"), techStacks.get("AWS"))
        );
        
        // 6. 당근마켓 - 중고거래 플랫폼 백엔드
        createJob(
            companies.get("당근마켓"),
            "중고거래 플랫폼 백엔드 개발자",
            "당근마켓의 중고거래 플랫폼을 개발할 백엔드 개발자를 모집합니다. 위치 기반 서비스와 실시간 채팅 시스템에 관심이 있으신 분을 환영합니다.",
            "• 3년 이상의 백엔드 개발 경험\n• Python, Django/FastAPI 개발 경험\n• PostgreSQL, Redis 사용 경험\n• RESTful API 설계 경험\n• 협업 도구 사용 경험",
            "• 실시간 메시징 시스템 개발 경험\n• 위치 기반 서비스 개발 경험\n• AWS 인프라 운영 경험\n• React Native 경험\n• 스타트업 경험",
            "서초",
            Job.JobType.FULLTIME,
            Job.ExperienceLevel.JUNIOR,
            "https://team.daangn.com/job/6",
            LocalDateTime.now().plusDays(65),
            Arrays.asList(techStacks.get("Python"), techStacks.get("PostgreSQL"), techStacks.get("Redis")),
            Arrays.asList(techStacks.get("AWS"), techStacks.get("Docker"))
        );

        // 7. 네이버 - 검색 플랫폼 개발자
        createJob(
            companies.get("네이버"),
            "검색 플랫폼 백엔드 개발자",
            "네이버 검색의 핵심 플랫폼을 개발할 백엔드 개발자를 모집합니다. 대규모 데이터 처리와 검색 엔진 개발에 열정이 있으신 분을 찾습니다.",
            "• 7년 이상의 백엔드 개발 경험\n• Java, C++ 중 하나 이상 전문성\n• 대규모 분산 시스템 설계 경험\n• 데이터 구조 및 알고리즘 깊은 이해\n• Linux 시스템 프로그래밍 경험",
            "• 검색 엔진 개발 경험\n• 머신러닝 적용 경험\n• 오픈소스 기여 경험\n• 논문 작성 경험\n• 글로벌 서비스 경험",
            "성남",
            Job.JobType.FULLTIME,
            Job.ExperienceLevel.SENIOR,
            "https://recruit.navercorp.com/job/7",
            LocalDateTime.now().plusDays(85),
            Arrays.asList(techStacks.get("Java"), techStacks.get("Spring Boot")),
            Arrays.asList(techStacks.get("Kafka"), techStacks.get("Kubernetes"))
        );

        // 8. 카카오뱅크 - 뱅킹 서비스 개발자
        createJob(
            companies.get("카카오뱅크"),
            "뱅킹 서비스 백엔드 개발자",
            "카카오뱅크의 혁신적인 뱅킹 서비스를 개발할 백엔드 개발자를 찾습니다. 금융 시스템의 안정성과 확장성에 관심이 있으신 분을 환영합니다.",
            "• 4년 이상의 서버 개발 경험\n• Java, Spring Boot 숙련도\n• RDBMS 설계 및 최적화 경험\n• 트랜잭션 처리에 대한 이해\n• 금융 도메인 관심",
            "• 금융권 시스템 개발 경험\n• MSA 전환 경험\n• Kafka 사용 경험\n• 보안 인증 경험\n• 24/7 서비스 운영 경험",
            "판교",
            Job.JobType.FULLTIME,
            Job.ExperienceLevel.SENIOR,
            "https://kakaobank.career/job/8",
            LocalDateTime.now().plusDays(70),
            Arrays.asList(techStacks.get("Java"), techStacks.get("Spring Boot"), techStacks.get("MySQL")),
            Arrays.asList(techStacks.get("Kafka"), techStacks.get("MSA"))
        );

        // 9. 야놀자 - 예약 시스템 개발자
        createJob(
            companies.get("야놀자"),
            "숙박 예약 시스템 백엔드 개발자",
            "야놀자의 글로벌 숙박 예약 플랫폼을 개발할 백엔드 개발자를 모집합니다. 복잡한 예약 시스템과 결제 처리에 도전하고 싶으신 분을 찾습니다.",
            "• 백엔드 개발 경험 (신입 가능)\n• Go 또는 Java 개발 능력\n• RESTful API 이해\n• 데이터베이스 기본 지식\n• 문제 해결 능력",
            "• 예약/결제 시스템 경험\n• 분산 트랜잭션 처리 경험\n• k8s 환경 경험\n• 글로벌 서비스 경험\n• 스타트업 경험",
            "강남",
            Job.JobType.FULLTIME,
            Job.ExperienceLevel.ANY,
            "https://yanolja.career/job/9",
            LocalDateTime.now().plusDays(60),
            Arrays.asList(techStacks.get("Go"), techStacks.get("MySQL")),
            Arrays.asList(techStacks.get("k8s"), techStacks.get("gRPC"))
        );

        // 10. 마켓컬리 - 물류 최적화 개발자
        createJob(
            companies.get("마켓컬리"),
            "물류 최적화 시스템 개발자",
            "마켓컬리의 새벽배송을 책임질 물류 최적화 시스템 개발자를 찾습니다. 알고리즘과 실시간 데이터 처리에 관심이 있으신 분을 환영합니다.",
            "• 5년 이상의 백엔드 개발 경험\n• Python 또는 Go 전문성\n• 알고리즘 및 자료구조 이해\n• 실시간 데이터 처리 경험\n• AWS 클라우드 경험",
            "• 물류/SCM 도메인 경험\n• 최적화 알고리즘 구현 경험\n• Kafka 스트리밍 경험\n• 머신러닝 적용 경험\n• DevOps 경험",
            "송파",
            Job.JobType.FULLTIME,
            Job.ExperienceLevel.SENIOR,
            "https://kurly.career/job/10",
            LocalDateTime.now().plusDays(75),
            Arrays.asList(techStacks.get("Python"), techStacks.get("Go"), techStacks.get("AWS")),
            Arrays.asList(techStacks.get("Kafka"), techStacks.get("Docker"))
        );

        log.info("채용공고 10개 생성 완료");
    }
    
    private void createJob(Company company, String title, String description, String requirements,
                          String preferred, String location, Job.JobType jobType,
                          Job.ExperienceLevel experienceLevel, String sourceUrl,
                          LocalDateTime expiresAt, List<TechStack> requiredTechStacks,
                          List<TechStack> preferredTechStacks) {
        
        Job job = Job.create(company, title, description, sourceUrl)
                .withDetails(requirements, preferred, jobType, experienceLevel, location,
                            LocalDateTime.now(), expiresAt);
        
        // 필수 기술 스택 추가
        for (TechStack techStack : requiredTechStacks) {
            job = job.addRequiredTechStack(techStack);
        }
        
        // 우대 기술 스택 추가
        for (TechStack techStack : preferredTechStacks) {
            job = job.addPreferredTechStack(techStack);
        }
        
        saveJobPort.saveJob(job);
    }
}