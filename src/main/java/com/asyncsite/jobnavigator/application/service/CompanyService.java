package com.asyncsite.jobnavigator.application.service;

import com.asyncsite.jobnavigator.adapter.in.web.dto.CompanyWithCountResponse;
import com.asyncsite.jobnavigator.application.port.in.GetCompaniesUseCase;
import com.asyncsite.jobnavigator.application.port.out.LoadCompanyPort;
import com.asyncsite.jobnavigator.application.port.out.LoadJobPort;
import com.asyncsite.jobnavigator.domain.Company;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 회사 관련 비즈니스 로직 구현
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CompanyService implements GetCompaniesUseCase {
    
    private final LoadCompanyPort loadCompanyPort;
    private final LoadJobPort loadJobPort;
    
    @Override
    public List<Company> getAllCompanies() {
        log.info("Getting all companies");
        return loadCompanyPort.loadAllCompanies();
    }
    
    @Override
    public List<Company> getActiveCompanies() {
        log.info("Getting active companies");
        return loadCompanyPort.loadActiveCompanies();
    }
    
    @Override
    public List<CompanyWithCountResponse> getCompaniesWithJobCount() {
        log.info("Getting companies with job count");
        
        // 모든 회사 조회
        List<Company> companies = loadCompanyPort.loadAllCompanies();
        
        // 회사별 채용공고 수 조회
        Map<Long, Integer> jobCountByCompany = loadJobPort.countActiveJobsByCompany();
        
        // 결과 매핑
        return companies.stream()
                .map(company -> CompanyWithCountResponse.builder()
                        .id(company.getId())
                        .name(company.getName())
                        .nameEn(company.getNameEn())
                        .jobCount(jobCountByCompany.getOrDefault(company.getId(), 0))
                        .build())
                .filter(response -> response.jobCount() > 0) // 채용공고가 있는 회사만 필터링
                .sorted((a, b) -> b.jobCount().compareTo(a.jobCount())) // 채용공고 수 기준 내림차순 정렬
                .collect(Collectors.toList());
    }
}