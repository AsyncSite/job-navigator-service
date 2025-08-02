package com.asyncsite.jobnavigator.adapter.out.persistence.repository;

import com.asyncsite.jobnavigator.adapter.out.persistence.entity.JobPostingJpaEntity;
import com.asyncsite.jobnavigator.domain.ExperienceCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface JobPostingRepository extends JpaRepository<JobPostingJpaEntity, Long> {
    
    Optional<JobPostingJpaEntity> findBySourceUrl(String sourceUrl);
    
    Page<JobPostingJpaEntity> findByIsActiveTrue(Pageable pageable);
    
    Page<JobPostingJpaEntity> findByCompanyIdAndIsActiveTrue(Long companyId, Pageable pageable);
    
    @Query("SELECT j FROM JobPostingJpaEntity j WHERE j.isActive = true " +
           "AND (:companyId IS NULL OR j.company.id = :companyId) " +
           "AND (:experienceCategory IS NULL OR j.experienceCategory = :experienceCategory) " +
           "AND (:jobType IS NULL OR j.jobType = :jobType) " +
           "AND (:keyword IS NULL OR LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(j.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<JobPostingJpaEntity> searchJobs(@Param("companyId") Long companyId,
                                        @Param("experienceCategory") ExperienceCategory experienceCategory,
                                        @Param("jobType") JobPostingJpaEntity.JobType jobType,
                                        @Param("keyword") String keyword,
                                        Pageable pageable);
    
    @Query("SELECT j FROM JobPostingJpaEntity j JOIN j.jobTechStacks jts " +
           "WHERE j.isActive = true AND jts.techStack.id IN :techStackIds " +
           "GROUP BY j")
    Page<JobPostingJpaEntity> findByTechStackIds(@Param("techStackIds") List<Long> techStackIds, Pageable pageable);
    
    List<JobPostingJpaEntity> findByExpiresAtBeforeAndIsActiveTrue(LocalDateTime dateTime);
    
    boolean existsBySourceUrl(String sourceUrl);
    
    long countByIsActiveTrue();
    
    @Query(value = "SELECT company_id, COUNT(*) FROM job_postings " +
           "WHERE is_active = true " +
           "GROUP BY company_id", nativeQuery = true)
    List<Object[]> countActiveJobsByCompany();
    
    @Query(value = "SELECT jts.tech_stack_id, COUNT(DISTINCT jp.id) " +
           "FROM job_tech_stacks jts " +
           "INNER JOIN job_postings jp ON jts.job_posting_id = jp.id " +
           "WHERE jp.is_active = true " +
           "GROUP BY jts.tech_stack_id", nativeQuery = true)
    List<Object[]> countActiveJobsByTechStack();
    
    @Query(value = "SELECT experience_category, COUNT(*) FROM job_postings " +
           "WHERE is_active = true " +
           "GROUP BY experience_category", nativeQuery = true)
    List<Object[]> countActiveJobsByExperienceCategory();
}