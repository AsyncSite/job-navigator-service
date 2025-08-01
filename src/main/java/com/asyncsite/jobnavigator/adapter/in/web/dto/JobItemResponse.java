package com.asyncsite.jobnavigator.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

/**
 * 프론트엔드 NavigatorList 컴포넌트와 일치하는 채용공고 응답 DTO
 */
@Builder
@Schema(description = "채용공고 아이템 응답")
public record JobItemResponse(
        @Schema(description = "채용공고 ID", example = "1")
        Long id,
        
        @Schema(description = "회사명", example = "네이버웹툰")
        String company,
        
        @Schema(description = "회사 로고 문자", example = "N")
        String companyLogo,
        
        @Schema(description = "채용공고 제목", example = "백엔드 서버 개발자 (네이버웹툰)")
        String title,
        
        @Schema(description = "채용공고 설명", example = "네이버웹툰의 글로벌 서비스를 함께 만들어갈 백엔드 개발자를 찾습니다.")
        String description,
        
        @Schema(description = "필요 기술 스택", example = "[\"Java\", \"Spring Boot\", \"Kotlin\", \"MSA\"]")
        List<String> skills,
        
        @Schema(description = "경력 요구사항", example = "경력 3년 이상")
        String experience,
        
        @Schema(description = "근무 위치", example = "분당")
        String location,
        
        @Schema(description = "마감일", example = "~2025.08.31")
        String deadline,
        
        @Schema(description = "매칭 점수", example = "95")
        Integer matchScore,
        
        @Schema(description = "작전회의실 활성화 여부", example = "true")
        Boolean hasWarRoom,
        
        @Schema(description = "작전회의실 참여자 수", example = "12")
        Integer warRoomCount
) {}