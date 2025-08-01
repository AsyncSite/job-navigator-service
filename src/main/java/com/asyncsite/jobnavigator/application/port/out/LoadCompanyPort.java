package com.asyncsite.jobnavigator.application.port.out;

import com.asyncsite.jobnavigator.domain.Company;
import java.util.List;
import java.util.Optional;

/**
 * 회사 조회 포트
 */
public interface LoadCompanyPort {
    
    /**
     * ID로 회사 조회
     * @param companyId 회사 ID
     * @return 회사 정보 (없으면 empty)
     */
    Optional<Company> loadCompany(Long companyId);
    
    /**
     * 이름으로 회사 조회
     * @param name 회사명
     * @return 회사 정보 (없으면 empty)
     */
    Optional<Company> loadCompanyByName(String name);
    
    /**
     * 모든 회사 조회
     * @return 회사 목록
     */
    List<Company> loadAllCompanies();
    
    /**
     * 활성 채용공고가 있는 회사 조회
     * @return 활성 회사 목록
     */
    List<Company> loadActiveCompanies();
}