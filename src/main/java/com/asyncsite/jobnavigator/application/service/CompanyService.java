package com.asyncsite.jobnavigator.application.service;

import com.asyncsite.jobnavigator.application.port.in.GetCompaniesUseCase;
import com.asyncsite.jobnavigator.application.port.out.LoadCompanyPort;
import com.asyncsite.jobnavigator.domain.Company;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 회사 관련 비즈니스 로직 구현
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CompanyService implements GetCompaniesUseCase {
    
    private final LoadCompanyPort loadCompanyPort;
    
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
}