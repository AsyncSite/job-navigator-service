package com.asyncsite.jobnavigator.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "crawl_logs",
       indexes = {
           @Index(name = "idx_company_status", columnList = "company_id, status"),
           @Index(name = "idx_created", columnList = "created_at DESC")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrawlLogJpaEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private CompanyJpaEntity company;
    
    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private CrawlStatus status;
    
    @Column(name = "jobs_found")
    @Builder.Default
    private Integer jobsFound = 0;
    
    @Column(name = "jobs_created")
    @Builder.Default
    private Integer jobsCreated = 0;
    
    @Column(name = "jobs_updated")
    @Builder.Default
    private Integer jobsUpdated = 0;
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    @Column(name = "started_at")
    private LocalDateTime startedAt;
    
    @Column(name = "finished_at")
    private LocalDateTime finishedAt;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    public enum CrawlStatus {
        SUCCESS, FAILED, PARTIAL
    }
}