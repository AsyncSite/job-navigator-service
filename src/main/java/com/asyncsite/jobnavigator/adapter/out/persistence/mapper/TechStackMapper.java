package com.asyncsite.jobnavigator.adapter.out.persistence.mapper;

import com.asyncsite.jobnavigator.adapter.out.persistence.entity.TechStackJpaEntity;
import com.asyncsite.jobnavigator.domain.TechStack;
import org.springframework.stereotype.Component;

/**
 * TechStack 도메인 모델과 JPA Entity 간 매핑
 */
@Component
public class TechStackMapper {
    
    /**
     * JPA Entity를 도메인 모델로 변환
     */
    public TechStack toDomain(TechStackJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        
        return TechStack.withId(
                entity.getId(),
                entity.getName(),
                TechStack.Category.valueOf(entity.getCategory().name())
        );
    }
    
    /**
     * 도메인 모델을 JPA Entity로 변환
     */
    public TechStackJpaEntity toEntity(TechStack domain) {
        if (domain == null) {
            return null;
        }
        
        TechStackJpaEntity entity = new TechStackJpaEntity();
        
        if (domain.getId() != null) {
            entity.setId(domain.getId());
        }
        
        entity.setName(domain.getName());
        entity.setCategory(TechStackJpaEntity.TechCategory.valueOf(domain.getCategory().name()));
        
        return entity;
    }
    
    /**
     * 기존 Entity에 도메인 모델 데이터 업데이트
     */
    public void updateEntity(TechStackJpaEntity entity, TechStack domain) {
        entity.setName(domain.getName());
        entity.setCategory(TechStackJpaEntity.TechCategory.valueOf(domain.getCategory().name()));
    }
}