package com.asyncsite.jobnavigator.application.port.out;

import com.asyncsite.jobnavigator.domain.Job;

/**
 * 채용공고 저장 포트
 */
public interface SaveJobPort {
    
    /**
     * 채용공고 저장
     * @param job 저장할 채용공고
     * @return 저장된 채용공고
     */
    Job saveJob(Job job);
    
    /**
     * 채용공고 업데이트
     * @param job 업데이트할 채용공고
     * @return 업데이트된 채용공고
     */
    Job updateJob(Job job);
}