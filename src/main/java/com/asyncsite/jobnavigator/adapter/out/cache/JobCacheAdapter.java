package com.asyncsite.jobnavigator.adapter.out.cache;

import com.asyncsite.jobnavigator.application.port.out.JobCachePort;
import com.asyncsite.jobnavigator.domain.Job;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * Redis 기반 채용공고 캐시 어댑터
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JobCacheAdapter implements JobCachePort {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper redisObjectMapper;
    
    private static final String JOB_CACHE_PREFIX = "job:";
    private static final String SEARCH_CACHE_PREFIX = "search:";
    private static final Duration JOB_CACHE_TTL = Duration.ofHours(1);
    private static final Duration SEARCH_CACHE_TTL = Duration.ofMinutes(10);
    
    @Override
    public Optional<Job> getCachedJob(Long jobId) {
        String key = JOB_CACHE_PREFIX + jobId;
        try {
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached != null) {
                Job job = redisObjectMapper.convertValue(cached, Job.class);
                log.debug("Cache hit for job: {}", jobId);
                return Optional.of(job);
            }
        } catch (Exception e) {
            log.error("Error getting cached job: {}", jobId, e);
        }
        return Optional.empty();
    }
    
    @Override
    public void cacheJob(Job job) {
        String key = JOB_CACHE_PREFIX + job.getId();
        try {
            redisTemplate.opsForValue().set(key, job, JOB_CACHE_TTL);
            log.debug("Cached job: {}", job.getId());
        } catch (Exception e) {
            log.error("Error caching job: {}", job.getId(), e);
        }
    }
    
    @Override
    public void cacheSearchResult(String cacheKey, List<Job> jobs) {
        String key = SEARCH_CACHE_PREFIX + cacheKey;
        try {
            redisTemplate.opsForValue().set(key, jobs, SEARCH_CACHE_TTL);
            log.debug("Cached search result with key: {}", cacheKey);
        } catch (Exception e) {
            log.error("Error caching search result: {}", cacheKey, e);
        }
    }
    
    @Override
    public Optional<List<Job>> getCachedSearchResult(String cacheKey) {
        String key = SEARCH_CACHE_PREFIX + cacheKey;
        try {
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached != null) {
                List<Job> jobs = redisObjectMapper.convertValue(cached, new TypeReference<List<Job>>() {});
                log.debug("Cache hit for search: {}", cacheKey);
                return Optional.of(jobs);
            }
        } catch (Exception e) {
            log.error("Error getting cached search result: {}", cacheKey, e);
        }
        return Optional.empty();
    }
    
    @Override
    public void evictJob(Long jobId) {
        String key = JOB_CACHE_PREFIX + jobId;
        try {
            redisTemplate.delete(key);
            log.debug("Evicted job from cache: {}", jobId);
        } catch (Exception e) {
            log.error("Error evicting job from cache: {}", jobId, e);
        }
    }
    
    @Override
    public void evictAll() {
        try {
            // Job 캐시 삭제
            redisTemplate.delete(redisTemplate.keys(JOB_CACHE_PREFIX + "*"));
            // Search 캐시 삭제
            redisTemplate.delete(redisTemplate.keys(SEARCH_CACHE_PREFIX + "*"));
            log.info("Evicted all caches");
        } catch (Exception e) {
            log.error("Error evicting all caches", e);
        }
    }
}