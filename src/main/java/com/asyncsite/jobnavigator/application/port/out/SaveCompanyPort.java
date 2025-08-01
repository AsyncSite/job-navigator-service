package com.asyncsite.jobnavigator.application.port.out;

import com.asyncsite.jobnavigator.domain.Company;

/**
 * 회사 저장 포트
 */
public interface SaveCompanyPort {
    
    /**
     * 회사 저장
     * @param company 저장할 회사
     * @return 저장된 회사
     */
    Company saveCompany(Company company);
}