package com.asyncsite.jobnavigator.adapter.out.persistence;

import com.asyncsite.jobnavigator.adapter.out.persistence.entity.JobPostingJpaEntity;
import com.asyncsite.jobnavigator.adapter.out.persistence.entity.JobTechStackJpaEntity;
import com.asyncsite.jobnavigator.adapter.out.persistence.entity.TechStackJpaEntity;
import com.asyncsite.jobnavigator.adapter.out.persistence.mapper.JobMapper;
import com.asyncsite.jobnavigator.adapter.out.persistence.mapper.TechStackMapper;
import com.asyncsite.jobnavigator.adapter.out.persistence.repository.JobJpaRepository;
import com.asyncsite.jobnavigator.adapter.out.persistence.repository.JobPostingRepository;
import com.asyncsite.jobnavigator.adapter.out.persistence.repository.JobTechStackJpaRepository;
import com.asyncsite.jobnavigator.adapter.out.persistence.repository.TechStackJpaRepository;
import com.asyncsite.jobnavigator.application.port.out.LoadJobPort;
import com.asyncsite.jobnavigator.application.port.out.SaveJobPort;
import com.asyncsite.jobnavigator.domain.Job;
import com.asyncsite.jobnavigator.domain.TechStack;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 채용공고 영속성 어댑터 구현
 */
@Component
@RequiredArgsConstructor
@Slf4j
@Transactional
public class JobPersistenceAdapter implements LoadJobPort, SaveJobPort {
    
    private final JobJpaRepository jobRepository;
    private final JobPostingRepository jobPostingRepository;
    private final TechStackJpaRepository techStackRepository;
    private final JobTechStackJpaRepository jobTechStackRepository;
    private final JobMapper jobMapper;
    private final TechStackMapper techStackMapper;
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Job> loadJob(Long jobId) {
        log.debug("Loading job with id: {}", jobId);
        return jobRepository.findByIdWithTechStacks(jobId)
                .map(jobMapper::toDomain);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Job> loadActiveJobs() {
        log.debug("Loading all active jobs");
        List<JobPostingJpaEntity> entities = jobRepository.findActiveJobsWithTechStacks();
        log.debug("Found {} active job entities", entities.size());
        
        return entities.stream()
                .map(entity -> {
                    log.debug("Job '{}' has {} tech stacks", entity.getTitle(), 
                            entity.getJobTechStacks() != null ? entity.getJobTechStacks().size() : 0);
                    Job job = jobMapper.toDomain(entity);
                    log.debug("Mapped job '{}' has {} required and {} preferred tech stacks", 
                            job.getTitle(), job.getRequiredTechStacks().size(), job.getPreferredTechStacks().size());
                    return job;
                })
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Job> loadJobsByCompany(Long companyId) {
        log.debug("Loading jobs for company: {}", companyId);
        return jobRepository.findByCompanyIdAndIsActiveTrue(companyId).stream()
                .map(jobMapper::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Job> loadJobsByTechStacks(List<Long> techStackIds) {
        log.debug("Loading jobs with tech stacks: {}", techStackIds);
        return jobRepository.findByTechStackIds(techStackIds).stream()
                .map(jobMapper::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Job> loadJobBySourceUrl(String sourceUrl) {
        log.debug("Loading job with source URL: {}", sourceUrl);
        return jobRepository.findBySourceUrl(sourceUrl)
                .map(jobMapper::toDomain);
    }
    
    @Override
    public Job saveJob(Job job) {
        log.debug("Saving job: {} with {} required and {} preferred tech stacks", 
                job.getTitle(), 
                job.getRequiredTechStacks() != null ? job.getRequiredTechStacks().size() : 0,
                job.getPreferredTechStacks() != null ? job.getPreferredTechStacks().size() : 0);
        
        JobPostingJpaEntity entity = jobMapper.toEntity(job);
        JobPostingJpaEntity saved = jobRepository.save(entity);
        log.debug("Saved job entity with ID: {}", saved.getId());
        
        // TechStack 연관관계 저장
        // 필수 기술 스택 저장
        if (job.getRequiredTechStacks() != null) {
            log.debug("Saving {} required tech stacks", job.getRequiredTechStacks().size());
            for (TechStack techStack : job.getRequiredTechStacks()) {
                TechStackJpaEntity techStackEntity = techStackRepository.findById(techStack.getId())
                        .orElseThrow(() -> new IllegalArgumentException("TechStack not found: " + techStack.getId()));
                
                JobTechStackJpaEntity jobTechStack = JobTechStackJpaEntity.builder()
                        .jobPosting(saved)
                        .techStack(techStackEntity)
                        .isRequired(true)
                        .build();
                
                jobTechStackRepository.save(jobTechStack);
                log.debug("Saved required tech stack: {}", techStack.getName());
            }
        }
        
        // 우대 기술 스택 저장
        if (job.getPreferredTechStacks() != null) {
            log.debug("Saving {} preferred tech stacks", job.getPreferredTechStacks().size());
            for (TechStack techStack : job.getPreferredTechStacks()) {
                TechStackJpaEntity techStackEntity = techStackRepository.findById(techStack.getId())
                        .orElseThrow(() -> new IllegalArgumentException("TechStack not found: " + techStack.getId()));
                
                JobTechStackJpaEntity jobTechStack = JobTechStackJpaEntity.builder()
                        .jobPosting(saved)
                        .techStack(techStackEntity)
                        .isRequired(false)
                        .build();
                
                jobTechStackRepository.save(jobTechStack);
                log.debug("Saved preferred tech stack: {}", techStack.getName());
            }
        }
        
        // 저장된 엔티티를 다시 로드하여 반환
        Job savedJob = loadJob(saved.getId()).orElseThrow(
                () -> new IllegalStateException("Failed to load saved job")
        );
        log.debug("Loaded saved job with {} required and {} preferred tech stacks",
                savedJob.getRequiredTechStacks().size(), savedJob.getPreferredTechStacks().size());
        return savedJob;
    }
    
    @Override
    public Job updateJob(Job job) {
        log.debug("Updating job: {}", job.getId());
        
        JobPostingJpaEntity existingEntity = jobRepository.findById(job.getId())
                .orElseThrow(() -> new IllegalArgumentException("Job not found with id: " + job.getId()));
        
        jobMapper.updateEntity(existingEntity, job);
        
        // TechStack 연관관계 처리는 JobTechStackJpaEntity를 통해
        // 별도로 처리해야 함 (Many-to-Many 관계)
        // 현재는 일단 업데이트만 처리
        
        JobPostingJpaEntity saved = jobRepository.save(existingEntity);
        return jobMapper.toDomain(saved);
    }
    
    @Override
    @Transactional(readOnly = true)
    public long countActiveJobs() {
        log.debug("Counting active jobs");
        return jobPostingRepository.countByIsActiveTrue();
    }
    
    @Override
    @Transactional(readOnly = true)
    public Map<Long, Integer> countActiveJobsByCompany() {
        log.debug("Counting active jobs by company");
        List<Object[]> results = jobPostingRepository.countActiveJobsByCompany();
        
        return results.stream()
                .collect(Collectors.toMap(
                        result -> (Long) result[0],
                        result -> ((Long) result[1]).intValue()
                ));
    }
    
    @Override
    @Transactional(readOnly = true)
    public Map<Long, Integer> countActiveJobsByTechStack() {
        log.debug("Counting active jobs by tech stack");
        List<Object[]> results = jobPostingRepository.countActiveJobsByTechStack();
        
        return results.stream()
                .collect(Collectors.toMap(
                        result -> (Long) result[0],
                        result -> ((Long) result[1]).intValue()
                ));
    }
}