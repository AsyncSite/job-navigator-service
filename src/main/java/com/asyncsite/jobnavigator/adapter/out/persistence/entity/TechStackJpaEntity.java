package com.asyncsite.jobnavigator.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tech_stacks",
       indexes = @Index(name = "idx_category", columnList = "category"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TechStackJpaEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;
    
    @Column(name = "category", length = 50)
    @Enumerated(EnumType.STRING)
    private TechCategory category;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @OneToMany(mappedBy = "techStack", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<JobTechStackJpaEntity> jobTechStacks = new ArrayList<>();
    
    public enum TechCategory {
        LANGUAGE, FRAMEWORK, DATABASE, TOOL, CLOUD, OTHER
    }
}