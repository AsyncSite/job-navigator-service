package com.asyncsite.jobnavigator.adapter.out.persistence;

import com.asyncsite.jobnavigator.adapter.out.persistence.mapper.TechStackMapper;
import com.asyncsite.jobnavigator.adapter.out.persistence.repository.TechStackJpaRepository;
import com.asyncsite.jobnavigator.application.port.out.LoadTechStackPort;
import com.asyncsite.jobnavigator.application.port.out.SaveTechStackPort;
import com.asyncsite.jobnavigator.domain.TechStack;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 기술 스택 영속성 어댑터 구현
 */
@Component
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TechStackPersistenceAdapter implements LoadTechStackPort, SaveTechStackPort {
    
    private final TechStackJpaRepository techStackRepository;
    private final TechStackMapper techStackMapper;
    
    @Override
    @Transactional(readOnly = true)
    public Optional<TechStack> loadTechStack(Long techStackId) {
        log.debug("Loading tech stack with id: {}", techStackId);
        return techStackRepository.findById(techStackId)
                .map(techStackMapper::toDomain);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<TechStack> loadTechStackByName(String name) {
        log.debug("Loading tech stack with name: {}", name);
        return techStackRepository.findByName(name)
                .map(techStackMapper::toDomain);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<TechStack> loadAllTechStacks() {
        log.debug("Loading all tech stacks");
        return techStackRepository.findAll().stream()
                .map(techStackMapper::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<TechStack> loadTechStacksByCategory(String category) {
        log.debug("Loading tech stacks by category: {}", category);
        return techStackRepository.findByCategory(category).stream()
                .map(techStackMapper::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public TechStack saveTechStack(TechStack techStack) {
        log.debug("Saving tech stack: {}", techStack.getName());
        
        var entity = techStackMapper.toEntity(techStack);
        var saved = techStackRepository.save(entity);
        
        return techStackMapper.toDomain(saved);
    }
}