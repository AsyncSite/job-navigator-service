package com.asyncsite.jobnavigator.application.port.in;

import com.asyncsite.jobnavigator.domain.Company;
import java.util.List;

/**
 * 회사 목록 조회 유스케이스
 */
public interface GetCompaniesUseCase {
    
    /**
     * 모든 회사 목록 조회
     * @return 회사 목록
     */
    List<Company> getAllCompanies();
    
    /**
     * 활성화된 채용공고가 있는 회사 목록 조회
     * @return 활성 회사 목록
     */
    List<Company> getActiveCompanies();
}