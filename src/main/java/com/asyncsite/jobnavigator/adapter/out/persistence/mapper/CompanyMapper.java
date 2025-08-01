package com.asyncsite.jobnavigator.adapter.out.persistence.mapper;

import com.asyncsite.jobnavigator.adapter.out.persistence.entity.CompanyJpaEntity;
import com.asyncsite.jobnavigator.domain.Company;
import org.springframework.stereotype.Component;

/**
 * Company 도메인 모델과 JPA Entity 간 매핑
 */
@Component
public class CompanyMapper {
    
    /**
     * JPA Entity를 도메인 모델로 변환
     */
    public Company toDomain(CompanyJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        
        return Company.withId(
                entity.getId(),
                entity.getName(),
                entity.getNameEn(),
                entity.getCareerPageUrl(),
                entity.getLogoUrl(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
    
    /**
     * 도메인 모델을 JPA Entity로 변환
     */
    public CompanyJpaEntity toEntity(Company domain) {
        if (domain == null) {
            return null;
        }
        
        CompanyJpaEntity entity = new CompanyJpaEntity();
        
        if (domain.getId() != null) {
            entity.setId(domain.getId());
        }
        
        entity.setName(domain.getName());
        entity.setNameEn(domain.getNameEn());
        entity.setCareerPageUrl(domain.getCareerPageUrl());
        entity.setLogoUrl(domain.getLogoUrl());
        
        return entity;
    }
    
    /**
     * 기존 Entity에 도메인 모델 데이터 업데이트
     */
    public void updateEntity(CompanyJpaEntity entity, Company domain) {
        entity.setName(domain.getName());
        entity.setNameEn(domain.getNameEn());
        entity.setCareerPageUrl(domain.getCareerPageUrl());
        entity.setLogoUrl(domain.getLogoUrl());
    }
}