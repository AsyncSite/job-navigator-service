package com.asyncsite.jobnavigator.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "job_tech_stacks",
       indexes = @Index(name = "idx_tech_stack", columnList = "tech_stack_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(JobTechStackId.class)
public class JobTechStackJpaEntity {
    
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_posting_id", nullable = false)
    private JobPostingJpaEntity jobPosting;
    
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tech_stack_id", nullable = false)
    private TechStackJpaEntity techStack;
    
    @Column(name = "is_required", nullable = false)
    @Builder.Default
    private Boolean isRequired = false;
}