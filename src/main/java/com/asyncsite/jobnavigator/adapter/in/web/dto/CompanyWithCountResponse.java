package com.asyncsite.jobnavigator.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

/**
 * 회사 정보와 채용공고 수를 포함하는 응답 DTO
 */
@Builder
@Schema(description = "회사 정보와 채용공고 수")
public record CompanyWithCountResponse(
        @Schema(description = "회사 ID", example = "1")
        Long id,
        
        @Schema(description = "회사명", example = "우아한형제들")
        String name,
        
        @Schema(description = "회사명 (영문)", example = "Woowahan Brothers")
        String nameEn,
        
        @Schema(description = "활성 채용공고 수", example = "12")
        Integer jobCount
) {}