package com.asyncsite.jobnavigator.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.*;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE, builderClassName = "JobBuilder")
@JsonDeserialize(builder = Job.JobBuilder.class)
public class Job {
    
    public enum JobType {
        FULLTIME("Full-time"),
        CONTRACT("Contract"),
        INTERN("Internship"),
        PARTTIME("Part-time");
        
        private final String displayName;
        
        JobType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    
    private final Long id;
    private final Company company;
    private final String title;
    private final String description;
    private final String requirements;
    private final String preferred;
    private final JobType jobType;
    private final String experienceRequirement;  // "5년 이상" 등 실제 텍스트
    private final ExperienceCategory experienceCategory;  // 내부 분류용
    private final String location;
    private final String sourceUrl;
    private final LocalDateTime postedAt;
    private final LocalDateTime expiresAt;
    private final boolean isActive;
    private final Set<TechStack> requiredTechStacks;
    private final Set<TechStack> preferredTechStacks;
    private final LocalDateTime crawledAt;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    
    public static Job create(Company company, String title, String description, String sourceUrl) {
        validateCompany(company);
        validateTitle(title);
        validateSourceUrl(sourceUrl);
        
        return Job.builder()
                .company(company)
                .title(title)
                .description(description)
                .sourceUrl(sourceUrl)
                .isActive(true)
                .requiredTechStacks(new HashSet<>())
                .preferredTechStacks(new HashSet<>())
                .crawledAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
    
    public Job withDetails(String requirements, String preferred, JobType jobType, 
                          String experienceRequirement, ExperienceCategory experienceCategory, String location,
                          LocalDateTime postedAt, LocalDateTime expiresAt) {
        return Job.builder()
                .id(this.id)
                .company(this.company)
                .title(this.title)
                .description(this.description)
                .requirements(requirements)
                .preferred(preferred)
                .jobType(jobType)
                .experienceRequirement(experienceRequirement)
                .experienceCategory(experienceCategory)
                .location(location)
                .sourceUrl(this.sourceUrl)
                .postedAt(postedAt)
                .expiresAt(expiresAt)
                .isActive(this.isActive)
                .requiredTechStacks(new HashSet<>(this.requiredTechStacks))
                .preferredTechStacks(new HashSet<>(this.preferredTechStacks))
                .crawledAt(this.crawledAt)
                .createdAt(this.createdAt)
                .updatedAt(LocalDateTime.now())
                .build();
    }
    
    public Job addRequiredTechStack(TechStack techStack) {
        if (techStack == null) {
            throw new IllegalArgumentException("TechStack cannot be null");
        }
        Set<TechStack> newRequiredTechStacks = new HashSet<>(this.requiredTechStacks);
        newRequiredTechStacks.add(techStack);
        
        return Job.builder()
                .id(this.id)
                .company(this.company)
                .title(this.title)
                .description(this.description)
                .requirements(this.requirements)
                .preferred(this.preferred)
                .jobType(this.jobType)
                .experienceRequirement(this.experienceRequirement)
                .experienceCategory(this.experienceCategory)
                .location(this.location)
                .sourceUrl(this.sourceUrl)
                .postedAt(this.postedAt)
                .expiresAt(this.expiresAt)
                .isActive(this.isActive)
                .requiredTechStacks(newRequiredTechStacks)
                .preferredTechStacks(new HashSet<>(this.preferredTechStacks))
                .crawledAt(this.crawledAt)
                .createdAt(this.createdAt)
                .updatedAt(LocalDateTime.now())
                .build();
    }
    
    public Job addPreferredTechStack(TechStack techStack) {
        if (techStack == null) {
            throw new IllegalArgumentException("TechStack cannot be null");
        }
        Set<TechStack> newPreferredTechStacks = new HashSet<>(this.preferredTechStacks);
        newPreferredTechStacks.add(techStack);
        
        return Job.builder()
                .id(this.id)
                .company(this.company)
                .title(this.title)
                .description(this.description)
                .requirements(this.requirements)
                .preferred(this.preferred)
                .jobType(this.jobType)
                .experienceRequirement(this.experienceRequirement)
                .experienceCategory(this.experienceCategory)
                .location(this.location)
                .sourceUrl(this.sourceUrl)
                .postedAt(this.postedAt)
                .expiresAt(this.expiresAt)
                .isActive(this.isActive)
                .requiredTechStacks(new HashSet<>(this.requiredTechStacks))
                .preferredTechStacks(newPreferredTechStacks)
                .crawledAt(this.crawledAt)
                .createdAt(this.createdAt)
                .updatedAt(LocalDateTime.now())
                .build();
    }
    
    public Job deactivate() {
        return Job.builder()
                .id(this.id)
                .company(this.company)
                .title(this.title)
                .description(this.description)
                .requirements(this.requirements)
                .preferred(this.preferred)
                .jobType(this.jobType)
                .experienceRequirement(this.experienceRequirement)
                .experienceCategory(this.experienceCategory)
                .location(this.location)
                .sourceUrl(this.sourceUrl)
                .postedAt(this.postedAt)
                .expiresAt(this.expiresAt)
                .isActive(false)
                .requiredTechStacks(new HashSet<>(this.requiredTechStacks))
                .preferredTechStacks(new HashSet<>(this.preferredTechStacks))
                .crawledAt(this.crawledAt)
                .createdAt(this.createdAt)
                .updatedAt(LocalDateTime.now())
                .build();
    }
    
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
    
    public double calculateMatchScore(Set<TechStack> userTechStacks) {
        if (userTechStacks == null || userTechStacks.isEmpty()) {
            return 0.0;
        }
        
        int requiredMatches = 0;
        for (TechStack required : requiredTechStacks) {
            if (userTechStacks.contains(required)) {
                requiredMatches++;
            }
        }
        
        int preferredMatches = 0;
        for (TechStack preferred : preferredTechStacks) {
            if (userTechStacks.contains(preferred)) {
                preferredMatches++;
            }
        }
        
        double requiredScore = requiredTechStacks.isEmpty() ? 1.0 : 
                              (double) requiredMatches / requiredTechStacks.size();
        double preferredScore = preferredTechStacks.isEmpty() ? 0.0 : 
                               (double) preferredMatches / preferredTechStacks.size();
        
        return (requiredScore * 0.7) + (preferredScore * 0.3);
    }
    
    /**
     * JPA Entity로부터 Job 도메인 모델 복원
     */
    public static Job fromEntity(
            Long id,
            Company company,
            String title,
            String description,
            String requirements,
            String preferred,
            JobType jobType,
            String experienceRequirement,
            ExperienceCategory experienceCategory,
            String location,
            String sourceUrl,
            LocalDateTime postedAt,
            LocalDateTime expiresAt,
            boolean isActive,
            Set<TechStack> requiredTechStacks,
            Set<TechStack> preferredTechStacks,
            LocalDateTime crawledAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        return Job.builder()
                .id(id)
                .company(company)
                .title(title)
                .description(description)
                .requirements(requirements)
                .preferred(preferred)
                .jobType(jobType)
                .experienceRequirement(experienceRequirement)
                .experienceCategory(experienceCategory)
                .location(location)
                .sourceUrl(sourceUrl)
                .postedAt(postedAt)
                .expiresAt(expiresAt)
                .isActive(isActive)
                .requiredTechStacks(requiredTechStacks != null ? requiredTechStacks : new HashSet<>())
                .preferredTechStacks(preferredTechStacks != null ? preferredTechStacks : new HashSet<>())
                .crawledAt(crawledAt)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }
    
    private static void validateCompany(Company company) {
        if (company == null) {
            throw new IllegalArgumentException("Company cannot be null");
        }
    }
    
    private static void validateTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Job title cannot be empty");
        }
        if (title.length() > 300) {
            throw new IllegalArgumentException("Job title cannot exceed 300 characters");
        }
    }
    
    private static void validateSourceUrl(String sourceUrl) {
        if (sourceUrl == null || sourceUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("Source URL cannot be empty");
        }
        if (sourceUrl.length() > 1000) {
            throw new IllegalArgumentException("Source URL cannot exceed 1000 characters");
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Job job = (Job) o;
        return Objects.equals(sourceUrl, job.sourceUrl);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(sourceUrl);
    }
    
    @JsonPOJOBuilder(withPrefix = "")
    public static class JobBuilder {
        // Lombok이 생성하는 builder 메서드들을 Jackson이 사용할 수 있도록 함
    }
}