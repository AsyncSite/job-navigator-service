package com.asyncsite.jobnavigator.application.port.out;

import com.asyncsite.jobnavigator.domain.Job;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 채용공고 조회 포트
 */
public interface LoadJobPort {
    
    /**
     * ID로 채용공고 조회
     * @param jobId 채용공고 ID
     * @return 채용공고 (없으면 empty)
     */
    Optional<Job> loadJob(Long jobId);
    
    /**
     * 활성화된 모든 채용공고 조회
     * @return 활성 채용공고 목록
     */
    List<Job> loadActiveJobs();
    
    /**
     * 회사별 채용공고 조회
     * @param companyId 회사 ID
     * @return 해당 회사의 채용공고 목록
     */
    List<Job> loadJobsByCompany(Long companyId);
    
    /**
     * 기술 스택으로 채용공고 조회
     * @param techStackIds 기술 스택 ID 목록
     * @return 해당 기술 스택을 포함한 채용공고 목록
     */
    List<Job> loadJobsByTechStacks(List<Long> techStackIds);
    
    /**
     * URL로 채용공고 조회
     * @param sourceUrl 원본 URL
     * @return 채용공고 (없으면 empty)
     */
    Optional<Job> loadJobBySourceUrl(String sourceUrl);
    
    /**
     * 활성 채용공고 개수 조회
     * @return 활성 채용공고 개수
     */
    long countActiveJobs();
    
    /**
     * 회사별 활성 채용공고 수 조회
     * @return 회사ID -> 채용공고 수 맵
     */
    Map<Long, Integer> countActiveJobsByCompany();
    
    /**
     * 기술스택별 활성 채용공고 수 조회
     * @return 기술스택ID -> 채용공고 수 맵
     */
    Map<Long, Integer> countActiveJobsByTechStack();
}