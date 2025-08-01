package com.asyncsite.jobnavigator.adapter.out.persistence.repository;

import com.asyncsite.jobnavigator.adapter.out.persistence.entity.UserSavedJobJpaEntity;
import com.asyncsite.jobnavigator.adapter.out.persistence.entity.UserSavedJobId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserSavedJobRepository extends JpaRepository<UserSavedJobJpaEntity, UserSavedJobId> {
    
    Page<UserSavedJobJpaEntity> findByUserId(Long userId, Pageable pageable);
    
    List<UserSavedJobJpaEntity> findByUserId(Long userId);
    
    boolean existsByUserIdAndJobPostingId(Long userId, Long jobPostingId);
    
    void deleteByUserIdAndJobPostingId(Long userId, Long jobPostingId);
}