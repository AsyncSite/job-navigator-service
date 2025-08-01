package com.asyncsite.jobnavigator.application.port.in;

import com.asyncsite.jobnavigator.domain.Job;

/**
 * 채용공고 상세 조회 유스케이스
 */
public interface GetJobDetailUseCase {
    
    /**
     * 채용공고 ID로 상세 정보 조회
     * @param jobId 채용공고 ID
     * @return 채용공고 상세 정보
     */
    Job getJobDetail(Long jobId);
}