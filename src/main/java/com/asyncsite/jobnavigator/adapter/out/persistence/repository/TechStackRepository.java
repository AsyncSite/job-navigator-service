package com.asyncsite.jobnavigator.adapter.out.persistence.repository;

import com.asyncsite.jobnavigator.adapter.out.persistence.entity.TechStackJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TechStackRepository extends JpaRepository<TechStackJpaEntity, Long> {
    
    Optional<TechStackJpaEntity> findByName(String name);
    
    List<TechStackJpaEntity> findByCategory(TechStackJpaEntity.TechCategory category);
    
    boolean existsByName(String name);
}