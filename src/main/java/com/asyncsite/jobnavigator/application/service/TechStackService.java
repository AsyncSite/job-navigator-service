package com.asyncsite.jobnavigator.application.service;

import com.asyncsite.jobnavigator.adapter.in.web.dto.TechStackWithCountResponse;
import com.asyncsite.jobnavigator.application.port.in.GetTechStacksUseCase;
import com.asyncsite.jobnavigator.application.port.out.LoadJobPort;
import com.asyncsite.jobnavigator.application.port.out.LoadTechStackPort;
import com.asyncsite.jobnavigator.domain.TechStack;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 기술 스택 관련 비즈니스 로직 구현
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TechStackService implements GetTechStacksUseCase {
    
    private final LoadTechStackPort loadTechStackPort;
    private final LoadJobPort loadJobPort;
    
    @Override
    public List<TechStack> getAllTechStacks() {
        log.info("Getting all tech stacks");
        return loadTechStackPort.loadAllTechStacks();
    }
    
    @Override
    public Map<String, List<TechStack>> getTechStacksByCategory() {
        log.info("Getting tech stacks by category");
        List<TechStack> allTechStacks = loadTechStackPort.loadAllTechStacks();
        
        return allTechStacks.stream()
                .collect(Collectors.groupingBy(ts -> ts.getCategory().name()));
    }
    
    @Override
    public List<TechStack> getPopularTechStacks(int limit) {
        log.info("Getting top {} popular tech stacks", limit);
        
        // TODO: 실제로는 채용공고 수를 기준으로 정렬해야 함
        // 현재는 임시로 전체 목록에서 limit만큼만 반환
        List<TechStack> allTechStacks = loadTechStackPort.loadAllTechStacks();
        
        return allTechStacks.stream()
                .limit(limit)
                .toList();
    }
    
    @Override
    public List<TechStackWithCountResponse> getTechStacksWithJobCount() {
        log.info("Getting tech stacks with job count");
        
        // 모든 기술 스택 조회
        List<TechStack> techStacks = loadTechStackPort.loadAllTechStacks();
        
        // 기술스택별 채용공고 수 조회
        Map<Long, Integer> jobCountByTechStack = loadJobPort.countActiveJobsByTechStack();
        
        // 결과 매핑
        return techStacks.stream()
                .map(techStack -> TechStackWithCountResponse.builder()
                        .id(techStack.getId())
                        .name(techStack.getName())
                        .category(techStack.getCategory().name())
                        .jobCount(jobCountByTechStack.getOrDefault(techStack.getId(), 0))
                        .build())
                .filter(response -> response.jobCount() > 0) // 채용공고가 있는 기술스택만 필터링
                .sorted((a, b) -> b.jobCount().compareTo(a.jobCount())) // 채용공고 수 기준 내림차순 정렬
                .collect(Collectors.toList());
    }
}