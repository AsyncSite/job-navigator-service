package com.asyncsite.jobnavigator.adapter.in.web;

import com.asyncsite.jobnavigator.adapter.in.web.dto.TechStackWithCountResponse;
import com.asyncsite.jobnavigator.application.port.in.GetTechStacksUseCase;
import com.asyncsite.jobnavigator.domain.TechStack;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 기술 스택 관련 REST API Controller
 */
@RestController
@RequestMapping("/api/tech-stacks")
@RequiredArgsConstructor
@Tag(name = "TechStack API", description = "기술 스택 관리 API")
public class TechStackController {
    
    private final GetTechStacksUseCase getTechStacksUseCase;
    
    @GetMapping
    @Operation(summary = "기술 스택 목록 조회", description = "모든 기술 스택 목록을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    public ResponseEntity<List<TechStack>> getAllTechStacks() {
        List<TechStack> techStacks = getTechStacksUseCase.getAllTechStacks();
        return ResponseEntity.ok(techStacks);
    }
    
    @GetMapping("/by-category")
    @Operation(summary = "카테고리별 기술 스택 조회", description = "카테고리별로 그룹화된 기술 스택을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    public ResponseEntity<Map<String, List<TechStack>>> getTechStacksByCategory() {
        Map<String, List<TechStack>> techStacksByCategory = getTechStacksUseCase.getTechStacksByCategory();
        return ResponseEntity.ok(techStacksByCategory);
    }
    
    @GetMapping("/popular")
    @Operation(summary = "인기 기술 스택 조회", description = "채용공고 수 기준 인기 기술 스택을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터")
    })
    public ResponseEntity<List<TechStack>> getPopularTechStacks(
            @Parameter(description = "조회할 개수", required = false)
            @RequestParam(defaultValue = "10") int limit
    ) {
        if (limit < 1 || limit > 100) {
            throw new IllegalArgumentException("Limit must be between 1 and 100");
        }
        
        List<TechStack> popularTechStacks = getTechStacksUseCase.getPopularTechStacks(limit);
        return ResponseEntity.ok(popularTechStacks);
    }
    
    @GetMapping("/with-count")
    @Operation(summary = "기술스택별 채용공고 수 조회", description = "각 기술스택별 활성 채용공고 수를 포함하여 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    public ResponseEntity<List<TechStackWithCountResponse>> getTechStacksWithJobCount() {
        List<TechStackWithCountResponse> techStacksWithCount = getTechStacksUseCase.getTechStacksWithJobCount();
        return ResponseEntity.ok(techStacksWithCount);
    }
}