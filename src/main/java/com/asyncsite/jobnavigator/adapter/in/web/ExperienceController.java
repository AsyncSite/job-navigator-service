package com.asyncsite.jobnavigator.adapter.in.web;

import com.asyncsite.jobnavigator.adapter.in.web.dto.ExperienceCategoryWithCountResponse;
import com.asyncsite.jobnavigator.adapter.out.persistence.repository.JobPostingRepository;
import com.asyncsite.jobnavigator.domain.ExperienceCategory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 경력 관련 REST API Controller
 */
@RestController
@RequestMapping("/api/experience")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Experience API", description = "경력 카테고리 관리 API")
public class ExperienceController {
    
    private final JobPostingRepository jobPostingRepository;
    
    @GetMapping("/categories/with-count")
    @Operation(summary = "경력 카테고리별 채용공고 개수 조회", description = "각 경력 카테고리별로 활성화된 채용공고 개수를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    public ResponseEntity<List<ExperienceCategoryWithCountResponse>> getExperienceCategoriesWithCount() {
        log.info("Fetching experience categories with job counts");
        
        // DB에서 카운트 조회
        List<Object[]> counts = jobPostingRepository.countActiveJobsByExperienceCategory();
        Map<String, Integer> countMap = counts.stream()
                .filter(row -> row[0] != null)  // null 카테고리 제외
                .collect(Collectors.toMap(
                        row -> row[0].toString(),
                        row -> ((Number) row[1]).intValue()
                ));
        
        // 모든 경력 카테고리에 대해 응답 생성
        List<ExperienceCategoryWithCountResponse> response = Arrays.stream(ExperienceCategory.values())
                .map(category -> ExperienceCategoryWithCountResponse.builder()
                        .category(category.name())
                        .displayName(category.getDisplayName())
                        .jobCount(countMap.getOrDefault(category.name(), 0))
                        .build())
                .collect(Collectors.toList());
        
        log.info("Found {} experience categories with counts", response.size());
        return ResponseEntity.ok(response);
    }
}