# Job Navigator Service - Batch API 구현 가이드

> 크롤러 서비스와 연동하기 위한 배치 저장 API 구현 가이드

## 📋 구현 필요 사항

### 1. SaveJobCommand 수정
```java
// 기존 SaveJobCommand에 companyWebsite 필드 추가
public record SaveJobCommand(
    String title,
    String companyName,
    String companyWebsite,  // 추가
    String description,
    String requirements,
    String preferred,
    String jobType,
    String experienceLevel,
    String location,
    String sourceUrl,
    LocalDateTime postedAt,
    LocalDateTime expiresAt,
    List<String> techStackNames
) {}
```

### 2. BatchSaveResponse DTO 생성
```java
// src/main/java/com/asyncsite/jobnavigator/application/dto/BatchSaveResponse.java
package com.asyncsite.jobnavigator.application.dto;

import lombok.Builder;

@Builder
public record BatchSaveResponse(
    int total,
    int created,
    int updated,
    int failed,
    List<String> errors
) {}
```

### 3. JobWebAdapter에 배치 엔드포인트 추가
```java
// src/main/java/com/asyncsite/jobnavigator/adapter/in/web/JobWebAdapter.java

@PostMapping("/batch")
@ResponseStatus(HttpStatus.CREATED)
public BatchSaveResponse saveBatch(@RequestBody @Valid List<SaveJobCommand> commands) {
    log.info("Saving batch of {} jobs", commands.size());
    
    int created = 0;
    int updated = 0;
    int failed = 0;
    List<String> errors = new ArrayList<>();
    
    for (SaveJobCommand command : commands) {
        try {
            // 중복 체크 - source_url 기준
            Optional<Job> existingJob = loadJobPort.loadJobBySourceUrl(command.sourceUrl());
            
            if (existingJob.isPresent()) {
                // 기존 공고 업데이트
                Job job = existingJob.get();
                // 업데이트 로직 구현
                updated++;
            } else {
                // 신규 공고 생성
                saveJobUseCase.saveJob(command);
                created++;
            }
        } catch (Exception e) {
            failed++;
            errors.add(String.format("Failed to save job '%s': %s", 
                command.title(), e.getMessage()));
            log.error("Failed to save job in batch", e);
        }
    }
    
    BatchSaveResponse response = BatchSaveResponse.builder()
        .total(commands.size())
        .created(created)
        .updated(updated)
        .failed(failed)
        .errors(errors)
        .build();
    
    log.info("Batch save completed: {}", response);
    
    return response;
}
```

### 4. LoadJobPort에 source_url 조회 메서드 추가
```java
// application/port/out/LoadJobPort.java
public interface LoadJobPort {
    // 기존 메서드들...
    
    Optional<Job> loadJobBySourceUrl(String sourceUrl);
}
```

### 5. JobPersistenceAdapter에 구현 추가
```java
// adapter/out/persistence/JobPersistenceAdapter.java
@Override
public Optional<Job> loadJobBySourceUrl(String sourceUrl) {
    return jobJpaRepository.findBySourceUrl(sourceUrl)
        .map(jobMapper::toDomain);
}
```

### 6. JobJpaRepository에 쿼리 메서드 추가
```java
// adapter/out/persistence/repository/JobJpaRepository.java
Optional<JobPostingJpaEntity> findBySourceUrl(String sourceUrl);
```

## 🔒 보안 고려사항

### 1. 인증 설정
```java
// 배치 API는 내부 서비스만 호출 가능하도록 제한
// SecurityConfig에서 설정 필요
.requestMatchers(HttpMethod.POST, "/api/job-navigator/jobs/batch").hasRole("CRAWLER")
```

### 2. Rate Limiting
```java
// 대량 요청 제한
@PostMapping("/batch")
@RateLimiter(name = "batch-api", fallbackMethod = "batchFallback")
public BatchSaveResponse saveBatch(@RequestBody List<SaveJobCommand> commands) {
    if (commands.size() > 100) {
        throw new IllegalArgumentException("Batch size cannot exceed 100");
    }
    // ...
}
```

## 🧪 테스트

### 1. 통합 테스트
```java
@Test
void testBatchSave() throws Exception {
    // Given
    List<SaveJobCommand> commands = List.of(
        new SaveJobCommand(
            "백엔드 개발자",
            "네이버",
            "https://www.naver.com",
            "설명...",
            "자격요건...",
            "우대사항...",
            "FULLTIME",
            "SENIOR",
            "서울",
            "https://careers.naver.com/job/1234",
            LocalDateTime.now(),
            LocalDateTime.now().plusDays(30),
            List.of("Java", "Spring")
        )
    );
    
    // When & Then
    mockMvc.perform(post("/api/job-navigator/jobs/batch")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(commands)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.created").value(1))
            .andExpect(jsonPath("$.failed").value(0));
}
```

### 2. 수동 테스트
```bash
# 배치 저장 테스트
curl -X POST http://localhost:8080/api/job-navigator/jobs/batch \
  -H "Content-Type: application/json" \
  -d '[
    {
      "title": "백엔드 개발자",
      "companyName": "네이버",
      "companyWebsite": "https://www.naver.com",
      "description": "네이버 백엔드 개발자 모집",
      "requirements": "Java, Spring 경험 3년 이상",
      "preferred": "MSA 경험",
      "jobType": "FULLTIME",
      "experienceLevel": "SENIOR",
      "location": "서울",
      "sourceUrl": "https://careers.naver.com/job/1234",
      "postedAt": "2025-08-01T10:00:00",
      "expiresAt": "2025-08-31T23:59:59",
      "techStackNames": ["Java", "Spring", "MySQL"]
    }
  ]'
```

## 📝 체크리스트

- [ ] SaveJobCommand에 companyWebsite 필드 추가
- [ ] BatchSaveResponse DTO 생성
- [ ] JobWebAdapter에 /batch 엔드포인트 추가
- [ ] LoadJobPort에 loadJobBySourceUrl 메서드 추가
- [ ] JobPersistenceAdapter에 구현 추가
- [ ] JobJpaRepository에 findBySourceUrl 추가
- [ ] 통합 테스트 작성
- [ ] API 문서 업데이트 (Swagger)

---

**작성일**: 2025-08-01  
**참고**: 크롤러 서비스 구현은 CRAWLER_SERVICE_GUIDE.md 참조