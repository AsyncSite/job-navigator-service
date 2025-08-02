package com.asyncsite.jobnavigator.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

/**
 * 경력 카테고리별 채용공고 개수 응답 DTO
 */
@Builder
@Schema(description = "경력 카테고리별 채용공고 개수")
public record ExperienceCategoryWithCountResponse(
        @Schema(description = "경력 카테고리 코드", example = "MID")
        String category,
        
        @Schema(description = "경력 카테고리 표시명", example = "미드레벨 (3-7년)")
        String displayName,
        
        @Schema(description = "해당 카테고리의 채용공고 수", example = "42")
        Integer jobCount
) {}