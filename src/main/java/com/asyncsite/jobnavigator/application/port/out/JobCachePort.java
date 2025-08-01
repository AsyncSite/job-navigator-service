package com.asyncsite.jobnavigator.application.port.out;

import com.asyncsite.jobnavigator.domain.Job;
import java.util.List;
import java.util.Optional;

/**
 * 채용공고 캐싱 포트
 */
public interface JobCachePort {
    
    /**
     * 캐시에서 채용공고 조회
     * @param jobId 채용공고 ID
     * @return 캐시된 채용공고 (없으면 empty)
     */
    Optional<Job> getCachedJob(Long jobId);
    
    /**
     * 채용공고 캐싱
     * @param job 캐싱할 채용공고
     */
    void cacheJob(Job job);
    
    /**
     * 검색 결과 캐싱
     * @param cacheKey 캐시 키
     * @param jobs 캐싱할 채용공고 목록
     */
    void cacheSearchResult(String cacheKey, List<Job> jobs);
    
    /**
     * 캐시된 검색 결과 조회
     * @param cacheKey 캐시 키
     * @return 캐시된 채용공고 목록 (없으면 empty)
     */
    Optional<List<Job>> getCachedSearchResult(String cacheKey);
    
    /**
     * 캐시 무효화
     * @param jobId 무효화할 채용공고 ID
     */
    void evictJob(Long jobId);
    
    /**
     * 전체 캐시 무효화
     */
    void evictAll();
}