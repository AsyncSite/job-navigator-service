package com.asyncsite.jobnavigator.domain;

public enum ExperienceCategory {
    ENTRY("신입"),       // 신입
    JUNIOR("주니어"),     // 주니어 (1-3년)
    MID("미드레벨"),      // 미드레벨 (3-7년)
    SENIOR("시니어"),     // 시니어 (7년+)
    LEAD("리드/수석급"),  // 리드/수석급
    ANY("경력무관");      // 경력무관
    
    private final String displayName;
    
    ExperienceCategory(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}