package com.asyncsite.jobnavigator.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

/**
 * 채용공고 검색 결과 응답 DTO
 * 프론트엔드의 페이지네이션 요구사항에 맞춤
 */
@Builder
@Schema(description = "채용공고 검색 결과")
public record JobSearchResponse(
        @Schema(description = "채용공고 목록")
        List<JobItemResponse> content,
        
        @Schema(description = "현재 페이지 번호 (0부터 시작)", example = "0")
        Integer page,
        
        @Schema(description = "페이지 크기", example = "20")
        Integer size,
        
        @Schema(description = "전체 결과 수", example = "152")
        Long totalElements,
        
        @Schema(description = "전체 페이지 수", example = "8")
        Integer totalPages,
        
        @Schema(description = "마지막 페이지 여부", example = "false")
        Boolean last,
        
        @Schema(description = "첫 페이지 여부", example = "true")
        Boolean first
) {}