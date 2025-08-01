package com.asyncsite.jobnavigator.application.port.out;

import com.asyncsite.jobnavigator.domain.TechStack;
import java.util.List;
import java.util.Optional;

/**
 * 기술 스택 조회 포트
 */
public interface LoadTechStackPort {
    
    /**
     * ID로 기술 스택 조회
     * @param techStackId 기술 스택 ID
     * @return 기술 스택 (없으면 empty)
     */
    Optional<TechStack> loadTechStack(Long techStackId);
    
    /**
     * 이름으로 기술 스택 조회
     * @param name 기술 스택명
     * @return 기술 스택 (없으면 empty)
     */
    Optional<TechStack> loadTechStackByName(String name);
    
    /**
     * 모든 기술 스택 조회
     * @return 기술 스택 목록
     */
    List<TechStack> loadAllTechStacks();
    
    /**
     * 카테고리별 기술 스택 조회
     * @param category 카테고리
     * @return 해당 카테고리의 기술 스택 목록
     */
    List<TechStack> loadTechStacksByCategory(String category);
}