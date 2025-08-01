package com.asyncsite.jobnavigator.application.port.out;

import com.asyncsite.jobnavigator.domain.TechStack;

/**
 * 기술 스택 저장 포트
 */
public interface SaveTechStackPort {
    
    /**
     * 기술 스택 저장
     * @param techStack 저장할 기술 스택
     * @return 저장된 기술 스택
     */
    TechStack saveTechStack(TechStack techStack);
}