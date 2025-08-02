package com.asyncsite.jobnavigator.application.port.in;

/**
 * 채용공고 저장 유스케이스 (크롤러 데이터 수신)
 */
public interface SaveJobUseCase {
    
    /**
     * 채용공고 저장
     * @param command 저장할 채용공고 정보
     * @return 저장된 채용공고 ID
     */
    Long saveJob(SaveJobCommand command);
    
    /**
     * 채용공고 저장 커맨드
     */
    record SaveJobCommand(
        String title,
        String description,
        String requirements,
        String preferred,
        String location,
        String jobType,
        String experienceCategory,  // ENTRY, JUNIOR, MID, SENIOR, LEAD, ANY
        String experienceRequirement,  // "5년 이상" 등 실제 텍스트
        String sourceUrl,
        String companyName,
        String companyWebsite,
        String companyLocation,
        java.util.List<String> techStackNames,
        java.time.LocalDateTime postedAt,
        java.time.LocalDateTime expiresAt,
        String rawHtml
    ) {}
}