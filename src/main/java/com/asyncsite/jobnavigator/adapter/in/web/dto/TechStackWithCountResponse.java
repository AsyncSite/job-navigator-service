package com.asyncsite.jobnavigator.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

/**
 * 기술 스택과 채용공고 수를 포함하는 응답 DTO
 */
@Builder
@Schema(description = "기술 스택과 채용공고 수")
public record TechStackWithCountResponse(
        @Schema(description = "기술 스택 ID", example = "1")
        Long id,
        
        @Schema(description = "기술 스택명", example = "Spring Boot")
        String name,
        
        @Schema(description = "카테고리", example = "FRAMEWORK")
        String category,
        
        @Schema(description = "활성 채용공고 수", example = "45")
        Integer jobCount
) {}