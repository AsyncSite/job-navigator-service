package com.asyncsite.jobnavigator.application.port.in;

import com.asyncsite.jobnavigator.domain.Job;
import java.util.List;

/**
 * 채용공고 검색 유스케이스
 */
public interface SearchJobsUseCase {
    
    /**
     * 채용공고 검색
     * @param command 검색 조건
     * @return 검색 결과
     */
    SearchJobsResult searchJobs(SearchJobsCommand command);
    
    /**
     * 검색 조건 커맨드
     */
    record SearchJobsCommand(
        String keyword,
        List<Long> companyIds,
        List<Long> techStackIds,
        String experienceLevel,
        String jobType,
        String location,
        Boolean isActive,
        int page,
        int size,
        String sortBy,
        String sortDirection
    ) {}
    
    /**
     * 검색 결과
     */
    record SearchJobsResult(
        List<Job> jobs,
        long totalElements,
        int totalPages,
        int currentPage,
        int pageSize
    ) {}
}