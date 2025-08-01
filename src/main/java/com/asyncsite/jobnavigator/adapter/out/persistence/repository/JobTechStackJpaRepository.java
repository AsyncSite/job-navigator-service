package com.asyncsite.jobnavigator.adapter.out.persistence.repository;

import com.asyncsite.jobnavigator.adapter.out.persistence.entity.JobTechStackJpaEntity;
import com.asyncsite.jobnavigator.adapter.out.persistence.entity.JobTechStackId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * JobTechStack JPA Repository
 */
@Repository
public interface JobTechStackJpaRepository extends JpaRepository<JobTechStackJpaEntity, JobTechStackId> {
    
    /**
     * 특정 채용공고의 모든 기술 스택 조회
     */
    List<JobTechStackJpaEntity> findByJobPostingId(Long jobPostingId);
    
    /**
     * 특정 채용공고의 기술 스택 삭제
     */
    void deleteByJobPostingId(Long jobPostingId);
}