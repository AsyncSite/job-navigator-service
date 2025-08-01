package com.asyncsite.jobnavigator.application.service;

import com.asyncsite.jobnavigator.application.port.in.GetTechStacksUseCase;
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
}