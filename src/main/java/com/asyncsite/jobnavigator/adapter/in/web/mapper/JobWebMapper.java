package com.asyncsite.jobnavigator.adapter.in.web.mapper;

import com.asyncsite.jobnavigator.adapter.in.web.dto.JobItemResponse;
import com.asyncsite.jobnavigator.adapter.in.web.dto.JobSearchResponse;
import com.asyncsite.jobnavigator.application.port.in.SearchJobsUseCase.SearchJobsResult;
import com.asyncsite.jobnavigator.domain.Job;
import com.asyncsite.jobnavigator.domain.TechStack;
import com.asyncsite.jobnavigator.domain.ExperienceCategory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 도메인 모델을 웹 응답 DTO로 변환하는 매퍼
 */
@Component
public class JobWebMapper {
    
    private static final DateTimeFormatter DEADLINE_FORMATTER = DateTimeFormatter.ofPattern("~yyyy.MM.dd");
    
    /**
     * Job 도메인 모델을 JobItemResponse DTO로 변환
     */
    public JobItemResponse toJobItemResponse(Job job) {
        return JobItemResponse.builder()
                .id(job.getId())
                .company(job.getCompany().getName())
                .companyLogo(extractCompanyLogo(job.getCompany().getName()))
                .title(job.getTitle())
                .description(job.getDescription())
                .skills(extractSkills(job))
                .experience(job.getExperienceRequirement() != null ? job.getExperienceRequirement() : formatExperienceCategory(job.getExperienceCategory()))
                .location(job.getLocation())
                .deadline(formatDeadline(job.getExpiresAt()))
                .matchScore(calculateMatchScore(job))
                .hasWarRoom(false)  // TODO: 추후 War Room 기능 구현 시 수정
                .warRoomCount(null) // TODO: 추후 War Room 기능 구현 시 수정
                .sourceUrl(job.getSourceUrl())
                .build();
    }
    
    /**
     * SearchJobsResult를 JobSearchResponse로 변환
     */
    public JobSearchResponse toJobSearchResponse(SearchJobsResult result) {
        List<JobItemResponse> jobItems = result.jobs().stream()
                .map(this::toJobItemResponse)
                .collect(Collectors.toList());
        
        return JobSearchResponse.builder()
                .content(jobItems)
                .page(result.currentPage())
                .size(result.pageSize())
                .totalElements(result.totalElements())
                .totalPages(result.totalPages())
                .last(result.currentPage() >= result.totalPages() - 1)
                .first(result.currentPage() == 0)
                .build();
    }
    
    /**
     * 회사명에서 첫 글자 추출 (로고 대체)
     */
    private String extractCompanyLogo(String companyName) {
        if (companyName == null || companyName.isEmpty()) {
            return "?";
        }
        
        // 영문 회사명이 있으면 영문 첫 글자 사용
        if (companyName.matches("^[A-Za-z].*")) {
            return companyName.substring(0, 1).toUpperCase();
        }
        
        // 한글 회사명 처리
        return switch (companyName) {
            case "네이버", "네이버웹툰" -> "N";
            case "카카오", "카카오페이" -> "K";
            case "쿠팡" -> "C";
            case "배달의민족", "배민" -> "B";
            case "토스" -> "T";
            case "당근마켓", "당근" -> "D";
            case "라인" -> "L";
            default -> companyName.substring(0, 1);
        };
    }
    
    /**
     * 기술 스택 추출
     */
    private List<String> extractSkills(Job job) {
        // requiredTechStacks와 preferredTechStacks 합치기
        List<String> skills = job.getRequiredTechStacks().stream()
                .map(TechStack::getName)
                .collect(Collectors.toList());
        
        // preferredTechStacks 추가 (중복 제거)
        job.getPreferredTechStacks().stream()
                .map(TechStack::getName)
                .filter(name -> !skills.contains(name))
                .forEach(skills::add);
        
        return skills;
    }
    
    /**
     * 경력 카테고리 포맷팅
     */
    private String formatExperienceCategory(ExperienceCategory category) {
        if (category == null) {
            return "경력 무관";
        }
        
        return switch (category) {
            case ENTRY -> "신입";
            case JUNIOR -> "경력 1-3년";
            case MID -> "경력 3-7년";
            case SENIOR -> "경력 7년 이상";
            case LEAD -> "경력 10년 이상";
            case ANY -> "경력 무관";
        };
    }
    
    /**
     * 마감일 포맷팅
     */
    private String formatDeadline(LocalDateTime expiresAt) {
        if (expiresAt == null) {
            return "상시채용";
        }
        
        return expiresAt.format(DEADLINE_FORMATTER);
    }
    
    /**
     * 매칭 점수 계산 (임시 구현)
     * TODO: 실제 사용자 프로필과 비교하여 계산하도록 개선
     */
    private Integer calculateMatchScore(Job job) {
        // 임시로 70-95 사이의 랜덤 값 반환
        return 70 + (int)(Math.random() * 26);
    }
}