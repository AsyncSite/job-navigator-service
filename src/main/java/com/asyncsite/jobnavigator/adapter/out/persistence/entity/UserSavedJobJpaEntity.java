package com.asyncsite.jobnavigator.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_saved_jobs",
       indexes = {
           @Index(name = "idx_user", columnList = "user_id"),
           @Index(name = "idx_saved_at", columnList = "saved_at DESC")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(UserSavedJobId.class)
public class UserSavedJobJpaEntity {
    
    @Id
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_posting_id", nullable = false)
    private JobPostingJpaEntity jobPosting;
    
    @CreationTimestamp
    @Column(name = "saved_at", nullable = false, updatable = false)
    private LocalDateTime savedAt;
}