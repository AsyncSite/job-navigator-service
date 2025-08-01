package com.asyncsite.jobnavigator.adapter.out.persistence.repository;

import com.asyncsite.jobnavigator.adapter.out.persistence.entity.CompanyJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 회사 JPA Repository
 */
@Repository
public interface CompanyJpaRepository extends JpaRepository<CompanyJpaEntity, Long> {
    
    /**
     * 회사명으로 조회
     */
    Optional<CompanyJpaEntity> findByName(String name);
    
    /**
     * 활성 채용공고가 있는 회사 조회
     */
    @Query("SELECT DISTINCT c FROM CompanyJpaEntity c " +
           "JOIN c.jobPostings j " +
           "WHERE j.isActive = true")
    List<CompanyJpaEntity> findCompaniesWithActiveJobs();
}