package com.asyncsite.jobnavigator.application.service;

import com.asyncsite.jobnavigator.adapter.in.web.GlobalExceptionHandler.DuplicateResourceException;
import com.asyncsite.jobnavigator.application.port.in.*;
import com.asyncsite.jobnavigator.application.port.out.*;
import com.asyncsite.jobnavigator.domain.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

/**
 * 채용공고 관련 비즈니스 로직 구현
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class JobService implements SearchJobsUseCase, GetJobDetailUseCase, SaveJobUseCase, EvictAllCachesUseCase {
    
    private final LoadJobPort loadJobPort;
    private final SaveJobPort saveJobPort;
    private final LoadCompanyPort loadCompanyPort;
    private final SaveCompanyPort saveCompanyPort;
    private final LoadTechStackPort loadTechStackPort;
    private final SaveTechStackPort saveTechStackPort;
    private final JobCachePort jobCachePort;
    
    @Override
    public SearchJobsResult searchJobs(SearchJobsCommand command) {
        // 입력값 검증
        validateSearchCommand(command);
        
        log.info("Searching jobs with criteria: {}", command);
        
        // 캐시 키 생성
        // String cacheKey = generateCacheKey(command);
        
        // 캐시에서 조회 - 필터링된 전체 결과를 캐시함
        // Optional<List<Job>> cachedResult = jobCachePort.getCachedSearchResult(cacheKey);
        // if (cachedResult.isPresent()) {
        //     log.debug("Cache hit for search: {}", cacheKey);
        //     List<Job> filteredJobs = cachedResult.get();
        //     
        //     // 페이징 처리
        //     int start = command.page() * command.size();
        //     int end = Math.min(start + command.size(), filteredJobs.size());
        //     List<Job> pagedJobs = start < filteredJobs.size() ? 
        //         filteredJobs.subList(start, end) : List.of();
        //     
        //     return new SearchJobsResult(
        //             pagedJobs,
        //             filteredJobs.size(),
        //             (int) Math.ceil((double) filteredJobs.size() / command.size()),
        //             command.page(),
        //             command.size()
        //     );
        // }
        
        // 모든 활성 채용공고 조회
        List<Job> allJobs = loadJobPort.loadActiveJobs();
        log.debug("Loaded {} jobs from database", allJobs.size());
        
        // 필터링 로직 (임시 구현)
        List<Job> filteredJobs = allJobs.stream()
                .filter(job -> {
                    if (command.keyword() != null && !command.keyword().isEmpty()) {
                        return job.getTitle().toLowerCase().contains(command.keyword().toLowerCase()) ||
                               job.getDescription().toLowerCase().contains(command.keyword().toLowerCase());
                    }
                    return true;
                })
                .filter(job -> {
                    if (command.companyIds() != null && !command.companyIds().isEmpty()) {
                        return command.companyIds().contains(job.getCompany().getId());
                    }
                    return true;
                })
                .filter(job -> {
                    if (command.experienceLevel() != null && !command.experienceLevel().isEmpty()) {
                        // experienceLevel should be enum name (ENTRY, JUNIOR, MID, etc.)
                        return job.getExperienceCategory() != null && 
                               job.getExperienceCategory().name().equals(command.experienceLevel());
                    }
                    return true;
                })
                .filter(job -> {
                    if (command.techStackIds() != null && !command.techStackIds().isEmpty()) {
                        // TechStack 필터링 로직 추가
                        Set<Long> jobTechStackIds = new HashSet<>();
                        if (job.getRequiredTechStacks() != null) {
                            job.getRequiredTechStacks().forEach(ts -> jobTechStackIds.add(ts.getId()));
                        }
                        if (job.getPreferredTechStacks() != null) {
                            job.getPreferredTechStacks().forEach(ts -> jobTechStackIds.add(ts.getId()));
                        }
                        // 요청된 TechStack ID 중 하나라도 포함되어 있으면 통과
                        return command.techStackIds().stream()
                                .anyMatch(jobTechStackIds::contains);
                    }
                    return true;
                })
                .filter(job -> {
                    if (command.isActive() != null) {
                        return job.isActive() == command.isActive();
                    }
                    return true;
                })
                .toList();
        
        // 페이징 처리 (임시 구현)
        int start = command.page() * command.size();
        int end = Math.min(start + command.size(), filteredJobs.size());
        List<Job> pagedJobs = filteredJobs.subList(start, end);
        
        // 캐시 저장 - 필터링된 전체 결과를 저장
        // jobCachePort.cacheSearchResult(cacheKey, filteredJobs);
        
        return new SearchJobsResult(
                pagedJobs,
                filteredJobs.size(),
                (int) Math.ceil((double) filteredJobs.size() / command.size()),
                command.page(),
                command.size()
        );
    }
    
    @Override
    public Job getJobDetail(Long jobId) {
        log.info("Getting job detail for id: {}", jobId);
        
        // 캐시에서 조회
        // Optional<Job> cachedJob = jobCachePort.getCachedJob(jobId);
        // if (cachedJob.isPresent()) {
        //     log.debug("Cache hit for job: {}", jobId);
        //     return cachedJob.get();
        // }
        
        // DB에서 조회
        Job job = loadJobPort.loadJob(jobId)
                .orElseThrow(() -> new NoSuchElementException("Job not found with id: " + jobId));
        
        // 캐시 저장
        // jobCachePort.cacheJob(job);
        
        return job;
    }
    
    @Override
    @Transactional
    public Long saveJob(SaveJobCommand command) {
        log.info("Saving job with title: {}", command.title());
        
        // 중복 체크 (source_url)
        Optional<Job> existingJob = loadJobPort.loadJobBySourceUrl(command.sourceUrl());
        if (existingJob.isPresent()) {
            throw new DuplicateResourceException("Job already exists with source URL: " + command.sourceUrl());
        }
        
        // 회사 조회 또는 생성
        Company company = loadCompanyPort.loadCompanyByName(command.companyName())
                .orElseGet(() -> {
                    Company newCompany = Company.create(
                            command.companyName(),
                            null, // nameEn
                            command.companyWebsite(), // careerPageUrl로 사용
                            null  // 로고 URL은 나중에 추가
                    );
                    return saveCompanyPort.saveCompany(newCompany);
                });
        
        // Job 생성 (기본 정보로 먼저 생성)
        Job job = Job.create(
                company,
                command.title(),
                command.description(),
                command.sourceUrl()
        );
        
        // 추가 정보 설정
        job = job.withDetails(
                command.requirements(),
                command.preferred(),
                Job.JobType.valueOf(command.jobType()),
                command.experienceRequirement(),
                ExperienceCategory.valueOf(command.experienceCategory()),
                command.location(),
                command.postedAt(),
                command.expiresAt()
        );
        
        // 기술 스택 추가
        if (command.techStackNames() != null) {
            for (String techStackName : command.techStackNames()) {
                TechStack techStack = loadTechStackPort.loadTechStackByName(techStackName)
                        .orElseGet(() -> {
                            TechStack newTechStack = TechStack.create(
                                    techStackName,
                                    TechStack.Category.OTHER // 기본 카테고리
                            );
                            return saveTechStackPort.saveTechStack(newTechStack);
                        });
                // 모든 기술 스택을 required로 추가 (나중에 분류 로직 추가 가능)
                job = job.addRequiredTechStack(techStack);
            }
        }
        
        // 저장
        Job savedJob = saveJobPort.saveJob(job);
        
        // 캐시 무효화
        // jobCachePort.evictAll();
        
        log.info("Job saved successfully with id: {}", savedJob.getId());
        return savedJob.getId();
    }

    @Override
    public void evictAllCaches() {
        // jobCachePort.evictAll();
        log.info("Cache is disabled - evictAllCaches called but no action taken.");
    }
    
    private String generateCacheKey(SearchJobsCommand command) {
        return String.format("search:%s:%s:%s:%s:%s:%s:%s:%d:%d:%s:%s",
                command.keyword() != null ? command.keyword() : "",
                command.companyIds() != null ? command.companyIds().toString() : "",
                command.techStackIds() != null ? command.techStackIds().toString() : "",
                command.experienceLevel() != null ? command.experienceLevel() : "",
                command.jobType() != null ? command.jobType() : "",
                command.location() != null ? command.location() : "",
                command.isActive(),
                command.page(),
                command.size(),
                command.sortBy(),
                command.sortDirection()
        );
    }
    
    private void validateSearchCommand(SearchJobsCommand command) {
        // 페이지 번호 검증
        if (command.page() < 0) {
            throw new IllegalArgumentException("페이지 번호는 0 이상이어야 합니다.");
        }
        
        // 페이지 크기 검증
        if (command.size() <= 0 || command.size() > 100) {
            throw new IllegalArgumentException("페이지 크기는 1과 100 사이여야 합니다.");
        }
        
        // 키워드 길이 검증
        if (command.keyword() != null && command.keyword().length() > 100) {
            throw new IllegalArgumentException("검색 키워드는 100자를 초과할 수 없습니다.");
        }
        
        // 정렬 방향 검증
        if (!"ASC".equals(command.sortDirection()) && !"DESC".equals(command.sortDirection())) {
            throw new IllegalArgumentException("정렬 방향은 ASC 또는 DESC여야 합니다.");
        }
    }
}