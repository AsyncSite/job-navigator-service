package com.asyncsite.jobnavigator.adapter.out.persistence.repository;

import com.asyncsite.jobnavigator.adapter.out.persistence.entity.CrawlLogJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CrawlLogRepository extends JpaRepository<CrawlLogJpaEntity, Long> {
    
    Page<CrawlLogJpaEntity> findByCompanyId(Long companyId, Pageable pageable);
    
    List<CrawlLogJpaEntity> findByCompanyIdAndStatus(Long companyId, CrawlLogJpaEntity.CrawlStatus status);
    
    List<CrawlLogJpaEntity> findByCreatedAtAfter(LocalDateTime dateTime);
    
    Page<CrawlLogJpaEntity> findByStatus(CrawlLogJpaEntity.CrawlStatus status, Pageable pageable);
}