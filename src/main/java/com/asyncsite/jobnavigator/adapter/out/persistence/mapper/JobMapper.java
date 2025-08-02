package com.asyncsite.jobnavigator.adapter.out.persistence.mapper;

import com.asyncsite.jobnavigator.adapter.out.persistence.entity.JobPostingJpaEntity;
import com.asyncsite.jobnavigator.adapter.out.persistence.entity.JobTechStackJpaEntity;
import com.asyncsite.jobnavigator.adapter.out.persistence.entity.TechStackJpaEntity;
import com.asyncsite.jobnavigator.domain.Job;
import com.asyncsite.jobnavigator.domain.TechStack;
import com.asyncsite.jobnavigator.domain.ExperienceCategory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Job 도메인 모델과 JPA Entity 간 매핑
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JobMapper {
    
    private final CompanyMapper companyMapper;
    private final TechStackMapper techStackMapper;
    
    /**
     * JPA Entity를 도메인 모델로 변환
     */
    public Job toDomain(JobPostingJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        
        // TechStack 매핑
        Set<TechStack> requiredTechStacks = new HashSet<>();
        Set<TechStack> preferredTechStacks = new HashSet<>();
        
        log.debug("Mapping tech stacks for job: {}", entity.getTitle());
        log.debug("JobTechStacks collection size: {}", 
                entity.getJobTechStacks() != null ? entity.getJobTechStacks().size() : "null");
        
        if (entity.getJobTechStacks() != null && !entity.getJobTechStacks().isEmpty()) {
            for (JobTechStackJpaEntity jobTechStack : entity.getJobTechStacks()) {
                if (jobTechStack.getTechStack() != null) {
                    TechStack techStack = techStackMapper.toDomain(jobTechStack.getTechStack());
                    log.debug("Mapped tech stack: {} (required: {})", 
                            techStack.getName(), jobTechStack.getIsRequired());
                    if (Boolean.TRUE.equals(jobTechStack.getIsRequired())) {
                        requiredTechStacks.add(techStack);
                    } else {
                        preferredTechStacks.add(techStack);
                    }
                }
            }
        }
        
        log.debug("Total mapped - required: {}, preferred: {}", 
                requiredTechStacks.size(), preferredTechStacks.size());
        
        // fromEntity 사용하여 전체 데이터로 Job 생성
        return Job.fromEntity(
                entity.getId(),
                companyMapper.toDomain(entity.getCompany()),
                entity.getTitle(),
                entity.getDescription(),
                entity.getRequirements(),
                entity.getPreferred(),
                entity.getJobType() != null ? Job.JobType.valueOf(entity.getJobType().name()) : null,
                entity.getExperienceRequirement(),
                entity.getExperienceCategory() != null ? ExperienceCategory.valueOf(entity.getExperienceCategory().name()) : null,
                entity.getLocation(),
                entity.getSourceUrl(),
                entity.getPostedAt(),
                entity.getExpiresAt(),
                entity.getIsActive(),
                requiredTechStacks,
                preferredTechStacks,
                entity.getCrawledAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
    
    /**
     * 도메인 모델을 JPA Entity로 변환
     */
    public JobPostingJpaEntity toEntity(Job domain) {
        if (domain == null) {
            return null;
        }
        
        JobPostingJpaEntity entity = new JobPostingJpaEntity();
        
        if (domain.getId() != null) {
            entity.setId(domain.getId());
        }
        
        updateEntityFromDomain(entity, domain);
        
        return entity;
    }
    
    /**
     * 기존 Entity에 도메인 모델 데이터 업데이트
     */
    public void updateEntity(JobPostingJpaEntity entity, Job domain) {
        updateEntityFromDomain(entity, domain);
    }
    
    private void updateEntityFromDomain(JobPostingJpaEntity entity, Job domain) {
        entity.setTitle(domain.getTitle());
        entity.setDescription(domain.getDescription());
        entity.setRequirements(domain.getRequirements());
        entity.setPreferred(domain.getPreferred());
        entity.setLocation(domain.getLocation());
        
        if (domain.getJobType() != null) {
            entity.setJobType(JobPostingJpaEntity.JobType.valueOf(domain.getJobType().name()));
        }
        
        entity.setExperienceRequirement(domain.getExperienceRequirement());
        if (domain.getExperienceCategory() != null) {
            entity.setExperienceCategory(ExperienceCategory.valueOf(domain.getExperienceCategory().name()));
        }
        
        entity.setSourceUrl(domain.getSourceUrl());
        entity.setCompany(companyMapper.toEntity(domain.getCompany()));
        entity.setPostedAt(domain.getPostedAt());
        entity.setExpiresAt(domain.getExpiresAt());
        entity.setCrawledAt(domain.getCrawledAt());
        entity.setIsActive(domain.isActive());
        // rawHtml은 도메인 모델에 없음
        
        // TechStack 관계 설정은 JobTechStackJpaEntity를 통해 별도로 처리해야 함
        // 이는 JobPersistenceAdapter에서 처리
    }
}