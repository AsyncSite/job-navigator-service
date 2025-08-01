package com.asyncsite.jobnavigator.adapter.in.web;

import com.asyncsite.jobnavigator.application.port.in.GetCompaniesUseCase;
import com.asyncsite.jobnavigator.domain.Company;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 회사 정보 관련 REST API Controller
 */
@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
@Tag(name = "Company API", description = "회사 정보 관리 API")
public class CompanyController {
    
    private final GetCompaniesUseCase getCompaniesUseCase;
    
    @GetMapping
    @Operation(summary = "회사 목록 조회", description = "모든 회사 목록을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    public ResponseEntity<List<Company>> getAllCompanies(
            @Parameter(description = "활성 채용공고가 있는 회사만 조회") 
            @RequestParam(required = false, defaultValue = "false") boolean activeOnly
    ) {
        List<Company> companies = activeOnly 
                ? getCompaniesUseCase.getActiveCompanies()
                : getCompaniesUseCase.getAllCompanies();
        
        return ResponseEntity.ok(companies);
    }
    
    @GetMapping("/active")
    @Operation(summary = "활성 회사 목록 조회", description = "현재 채용공고가 있는 회사 목록을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    public ResponseEntity<List<Company>> getActiveCompanies() {
        List<Company> companies = getCompaniesUseCase.getActiveCompanies();
        return ResponseEntity.ok(companies);
    }
}