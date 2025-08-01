package com.asyncsite.jobnavigator.adapter.out.persistence.repository;

import com.asyncsite.jobnavigator.adapter.out.persistence.entity.CompanyJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<CompanyJpaEntity, Long> {
    
    Optional<CompanyJpaEntity> findByName(String name);
    
    boolean existsByName(String name);
}