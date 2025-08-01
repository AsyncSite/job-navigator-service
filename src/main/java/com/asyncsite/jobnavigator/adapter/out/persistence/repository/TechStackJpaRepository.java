package com.asyncsite.jobnavigator.adapter.out.persistence.repository;

import com.asyncsite.jobnavigator.adapter.out.persistence.entity.TechStackJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 기술 스택 JPA Repository
 */
@Repository
public interface TechStackJpaRepository extends JpaRepository<TechStackJpaEntity, Long> {
    
    /**
     * 이름으로 기술 스택 조회
     */
    Optional<TechStackJpaEntity> findByName(String name);
    
    /**
     * 카테고리별 기술 스택 조회
     */
    List<TechStackJpaEntity> findByCategory(String category);
    
    /**
     * 채용공고 수가 많은 인기 기술 스택 조회
     */
    @Query("SELECT ts FROM TechStackJpaEntity ts " +
           "LEFT JOIN ts.jobTechStacks jts " +
           "LEFT JOIN jts.jobPosting j " +
           "WHERE j.isActive = true " +
           "GROUP BY ts " +
           "ORDER BY COUNT(j) DESC")
    List<TechStackJpaEntity> findPopularTechStacks();
}