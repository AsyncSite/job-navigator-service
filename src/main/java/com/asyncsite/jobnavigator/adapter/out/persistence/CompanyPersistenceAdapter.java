package com.asyncsite.jobnavigator.adapter.out.persistence;

import com.asyncsite.jobnavigator.adapter.out.persistence.mapper.CompanyMapper;
import com.asyncsite.jobnavigator.adapter.out.persistence.repository.CompanyJpaRepository;
import com.asyncsite.jobnavigator.application.port.out.LoadCompanyPort;
import com.asyncsite.jobnavigator.application.port.out.SaveCompanyPort;
import com.asyncsite.jobnavigator.domain.Company;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 회사 영속성 어댑터 구현
 */
@Component
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CompanyPersistenceAdapter implements LoadCompanyPort, SaveCompanyPort {
    
    private final CompanyJpaRepository companyRepository;
    private final CompanyMapper companyMapper;
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Company> loadCompany(Long companyId) {
        log.debug("Loading company with id: {}", companyId);
        return companyRepository.findById(companyId)
                .map(companyMapper::toDomain);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Company> loadCompanyByName(String name) {
        log.debug("Loading company with name: {}", name);
        return companyRepository.findByName(name)
                .map(companyMapper::toDomain);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Company> loadAllCompanies() {
        log.debug("Loading all companies");
        return companyRepository.findAll().stream()
                .map(companyMapper::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Company> loadActiveCompanies() {
        log.debug("Loading companies with active jobs");
        return companyRepository.findCompaniesWithActiveJobs().stream()
                .map(companyMapper::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public Company saveCompany(Company company) {
        log.debug("Saving company: {}", company.getName());
        
        var entity = companyMapper.toEntity(company);
        var saved = companyRepository.save(entity);
        
        return companyMapper.toDomain(saved);
    }
}