package com.asyncsite.jobnavigator.adapter.in.web;

import com.asyncsite.jobnavigator.adapter.in.web.dto.JobItemResponse;
import com.asyncsite.jobnavigator.adapter.in.web.dto.JobSearchResponse;
import com.asyncsite.jobnavigator.adapter.in.web.mapper.JobWebMapper;
import com.asyncsite.jobnavigator.application.port.in.GetJobDetailUseCase;
import com.asyncsite.jobnavigator.application.port.in.SaveJobUseCase;
import com.asyncsite.jobnavigator.application.port.in.SearchJobsUseCase;
import com.asyncsite.jobnavigator.application.port.in.SearchJobsUseCase.SearchJobsCommand;
import com.asyncsite.jobnavigator.application.port.in.SearchJobsUseCase.SearchJobsResult;
import com.asyncsite.jobnavigator.application.port.in.SaveJobUseCase.SaveJobCommand;
import com.asyncsite.jobnavigator.application.port.in.EvictAllCachesUseCase;
import com.asyncsite.jobnavigator.domain.Job;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 채용공고 관련 REST API Controller
 */
@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
@Tag(name = "Job API", description = "채용공고 관리 API")
public class JobController {
    
    private final SearchJobsUseCase searchJobsUseCase;
    private final GetJobDetailUseCase getJobDetailUseCase;
    private final SaveJobUseCase saveJobUseCase;
    private final EvictAllCachesUseCase evictAllCachesUseCase;
    private final JobWebMapper jobWebMapper;
    
    @GetMapping
    @Operation(summary = "채용공고 검색", description = "조건에 맞는 채용공고를 검색합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "검색 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터")
    })
    public ResponseEntity<JobSearchResponse> searchJobs(
            @Parameter(description = "검색 키워드") @RequestParam(required = false) String keyword,
            @Parameter(description = "회사 ID 목록") @RequestParam(required = false) List<Long> companyIds,
            @Parameter(description = "기술 스택 ID 목록") @RequestParam(required = false) List<Long> techStackIds,
            @Parameter(description = "경력 수준") @RequestParam(required = false) String experienceLevel,
            @Parameter(description = "고용 형태") @RequestParam(required = false) String jobType,
            @Parameter(description = "근무 지역") @RequestParam(required = false) String location,
            @Parameter(description = "활성 상태") @RequestParam(required = false, defaultValue = "true") Boolean isActive,
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "정렬 기준") @RequestParam(defaultValue = "postedAt") String sortBy,
            @Parameter(description = "정렬 방향 (ASC/DESC)") @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        SearchJobsCommand command = new SearchJobsCommand(
                keyword, companyIds, techStackIds, experienceLevel, jobType,
                location, isActive, page, size, sortBy, sortDirection
        );
        
        SearchJobsResult result = searchJobsUseCase.searchJobs(command);
        JobSearchResponse response = jobWebMapper.toJobSearchResponse(result);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "채용공고 상세 조회", description = "ID로 채용공고 상세 정보를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "404", description = "채용공고를 찾을 수 없음")
    })
    public ResponseEntity<JobItemResponse> getJobDetail(
            @Parameter(description = "채용공고 ID", required = true) @PathVariable Long id
    ) {
        Job job = getJobDetailUseCase.getJobDetail(id);
        JobItemResponse response = jobWebMapper.toJobItemResponse(job);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/batch")
    @Operation(summary = "채용공고 일괄 등록 (내부용)", description = "크롤러로부터 채용공고 데이터를 수신하여 저장합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "저장 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @ApiResponse(responseCode = "409", description = "중복된 채용공고 (source_url)")
    })
    public ResponseEntity<JobBatchResponse> saveJobBatch(
            @RequestBody List<JobBatchRequest> requests
    ) {
        List<Long> savedJobIds = requests.stream()
                .map(request -> {
                    // Use experienceCategory if provided, otherwise fall back to experienceLevel for backward compatibility
                    String expCategory = request.experienceCategory() != null ? 
                            request.experienceCategory() : request.experienceLevel();
                    
                    SaveJobCommand command = new SaveJobCommand(
                            request.title(),
                            request.description(),
                            request.requirements(),
                            request.preferred(),
                            request.location(),
                            request.jobType(),
                            expCategory,  // Pass experience category/level
                            request.experienceRequirement(),  // New field
                            request.sourceUrl(),
                            request.companyName(),
                            request.companyWebsite(),
                            request.companyLocation(),
                            request.techStackNames(),
                            request.postedAt(),
                            request.expiresAt(),
                            request.rawHtml()
                    );
                    return saveJobUseCase.saveJob(command);
                })
                .toList();
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new JobBatchResponse(savedJobIds.size(), savedJobIds));
    }
    
    /**
     * 채용공고 일괄 등록 요청 DTO
     */
    public record JobBatchRequest(
            String title,
            String description,
            String requirements,
            String preferred,
            String location,
            String jobType,
            String experienceLevel,  // Deprecated - for backward compatibility
            String experienceRequirement,  // "5년 이상" 등 실제 텍스트
            String experienceCategory,     // ENTRY, JUNIOR, MID, SENIOR, LEAD, ANY
            String sourceUrl,
            String companyName,
            String companyWebsite,
            String companyLocation,
            List<String> techStackNames,
            java.time.LocalDateTime postedAt,
            java.time.LocalDateTime expiresAt,
            String rawHtml
    ) {}
    
    /**
     * 채용공고 일괄 등록 응답 DTO
     */
    public record JobBatchResponse(
            int savedCount,
            List<Long> jobIds
    ) {}

    @DeleteMapping("/cache")
    @Operation(summary = "채용공고 캐시 전체 삭제", description = "모든 채용공고 관련 캐시를 삭제합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "캐시 삭제 성공")
    })
    public ResponseEntity<Void> clearCache() {
        evictAllCachesUseCase.evictAllCaches();
        return ResponseEntity.noContent().build();
    }
}