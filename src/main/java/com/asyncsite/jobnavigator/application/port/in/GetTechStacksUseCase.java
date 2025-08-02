package com.asyncsite.jobnavigator.application.port.in;

import com.asyncsite.jobnavigator.adapter.in.web.dto.TechStackWithCountResponse;
import com.asyncsite.jobnavigator.domain.TechStack;
import java.util.List;
import java.util.Map;

/**
 * 기술 스택 조회 유스케이스
 */
public interface GetTechStacksUseCase {
    
    /**
     * 모든 기술 스택 목록 조회
     * @return 기술 스택 목록
     */
    List<TechStack> getAllTechStacks();
    
    /**
     * 카테고리별 기술 스택 목록 조회
     * @return 카테고리별 기술 스택 맵
     */
    Map<String, List<TechStack>> getTechStacksByCategory();
    
    /**
     * 인기 기술 스택 조회 (채용공고 수 기준)
     * @param limit 조회할 개수
     * @return 인기 기술 스택 목록
     */
    List<TechStack> getPopularTechStacks(int limit);
    
    /**
     * 기술스택별 채용공고 수를 포함한 목록 조회
     * @return 기술스택별 채용공고 수 목록
     */
    List<TechStackWithCountResponse> getTechStacksWithJobCount();
}