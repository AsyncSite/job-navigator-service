package com.asyncsite.jobnavigator.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "job_postings",
       indexes = {
           @Index(name = "idx_company", columnList = "company_id"),
           @Index(name = "idx_active", columnList = "is_active"),
           @Index(name = "idx_posted", columnList = "posted_at DESC"),
           @Index(name = "idx_created", columnList = "created_at DESC"),
           @Index(name = "idx_source_url", columnList = "source_url", unique = true)
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobPostingJpaEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private CompanyJpaEntity company;
    
    @Column(name = "title", nullable = false, length = 300)
    private String title;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "requirements", columnDefinition = "TEXT")
    private String requirements;
    
    @Column(name = "preferred", columnDefinition = "TEXT")
    private String preferred;
    
    @Column(name = "job_type", length = 50)
    @Enumerated(EnumType.STRING)
    private JobType jobType;
    
    @Column(name = "experience_level", length = 50)
    @Enumerated(EnumType.STRING)
    private ExperienceLevel experienceLevel;
    
    @Column(name = "location", length = 200)
    private String location;
    
    @Column(name = "source_url", nullable = false, unique = true, length = 1000)
    private String sourceUrl;
    
    @Column(name = "posted_at")
    private LocalDateTime postedAt;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
    
    @Column(name = "raw_html", columnDefinition = "LONGTEXT")
    private String rawHtml;
    
    @Column(name = "crawled_at")
    @Builder.Default
    private LocalDateTime crawledAt = LocalDateTime.now();
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "jobPosting", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<JobTechStackJpaEntity> jobTechStacks = new ArrayList<>();
    
    @OneToMany(mappedBy = "jobPosting", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<UserSavedJobJpaEntity> userSavedJobs = new ArrayList<>();
    
    public enum JobType {
        FULLTIME, CONTRACT, INTERN, PARTTIME
    }
    
    public enum ExperienceLevel {
        JUNIOR, MID, SENIOR, LEAD, ANY
    }
}