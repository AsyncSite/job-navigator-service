package com.asyncsite.jobnavigator.adapter.out.persistence.repository;

import com.asyncsite.jobnavigator.adapter.out.persistence.entity.JobPostingJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 채용공고 JPA Repository
 */
@Repository
public interface JobJpaRepository extends JpaRepository<JobPostingJpaEntity, Long> {
    
    /**
     * 활성화된 채용공고 조회
     */
    List<JobPostingJpaEntity> findByIsActiveTrue();
    
    /**
     * 활성화된 채용공고 조회 (TechStack 포함)
     */
    @Query("SELECT DISTINCT j FROM JobPostingJpaEntity j " +
           "LEFT JOIN FETCH j.company c " +
           "LEFT JOIN FETCH j.jobTechStacks jts " +
           "LEFT JOIN FETCH jts.techStack ts " +
           "WHERE j.isActive = true")
    List<JobPostingJpaEntity> findActiveJobsWithTechStacks();
    
    /**
     * 회사별 채용공고 조회
     */
    List<JobPostingJpaEntity> findByCompanyId(Long companyId);
    
    /**
     * 회사별 활성화된 채용공고 조회
     */
    List<JobPostingJpaEntity> findByCompanyIdAndIsActiveTrue(Long companyId);
    
    /**
     * 원본 URL로 채용공고 조회
     */
    Optional<JobPostingJpaEntity> findBySourceUrl(String sourceUrl);
    
    /**
     * ID로 채용공고 조회 (TechStack 포함)
     */
    @Query("SELECT j FROM JobPostingJpaEntity j " +
           "LEFT JOIN FETCH j.company c " +
           "LEFT JOIN FETCH j.jobTechStacks jts " +
           "LEFT JOIN FETCH jts.techStack ts " +
           "WHERE j.id = :id")
    Optional<JobPostingJpaEntity> findByIdWithTechStacks(@Param("id") Long id);
    
    /**
     * 기술 스택을 포함한 채용공고 조회
     */
    @Query("SELECT DISTINCT j FROM JobPostingJpaEntity j " +
           "JOIN j.jobTechStacks jts " +
           "JOIN jts.techStack ts " +
           "WHERE ts.id IN :techStackIds AND j.isActive = true")
    List<JobPostingJpaEntity> findByTechStackIds(@Param("techStackIds") List<Long> techStackIds);
    
    /**
     * 키워드로 채용공고 검색
     */
    @Query("SELECT j FROM JobPostingJpaEntity j " +
           "WHERE j.isActive = true AND " +
           "(LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(j.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<JobPostingJpaEntity> searchByKeyword(@Param("keyword") String keyword);
}