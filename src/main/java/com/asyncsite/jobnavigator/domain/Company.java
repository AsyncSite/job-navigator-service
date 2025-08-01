package com.asyncsite.jobnavigator.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Company {
    
    private final Long id;
    private final String name;
    private final String nameEn;
    private final String careerPageUrl;
    private final String logoUrl;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    
    public static Company create(String name, String nameEn, String careerPageUrl, String logoUrl) {
        validateName(name);
        return new Company(null, name, nameEn, careerPageUrl, logoUrl, LocalDateTime.now(), LocalDateTime.now());
    }
    
    @JsonCreator
    public static Company withId(@JsonProperty("id") Long id, 
                                 @JsonProperty("name") String name, 
                                 @JsonProperty("nameEn") String nameEn, 
                                 @JsonProperty("careerPageUrl") String careerPageUrl, 
                                 @JsonProperty("logoUrl") String logoUrl, 
                                 @JsonProperty("createdAt") LocalDateTime createdAt, 
                                 @JsonProperty("updatedAt") LocalDateTime updatedAt) {
        validateName(name);
        return new Company(id, name, nameEn, careerPageUrl, logoUrl, createdAt, updatedAt);
    }
    
    public Company updateInfo(String nameEn, String careerPageUrl, String logoUrl) {
        return new Company(this.id, this.name, nameEn, careerPageUrl, logoUrl, 
                          this.createdAt, LocalDateTime.now());
    }
    
    private static void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Company name cannot be empty");
        }
        if (name.length() > 100) {
            throw new IllegalArgumentException("Company name cannot exceed 100 characters");
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Company company = (Company) o;
        return Objects.equals(id, company.id) && Objects.equals(name, company.name);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}