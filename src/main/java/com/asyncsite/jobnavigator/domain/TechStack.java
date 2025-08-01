package com.asyncsite.jobnavigator.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TechStack {
    
    public enum Category {
        LANGUAGE("Programming Language"),
        FRAMEWORK("Framework"),
        DATABASE("Database"),
        TOOL("Development Tool"),
        CLOUD("Cloud Platform"),
        OTHER("Other");
        
        private final String displayName;
        
        Category(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    private final Long id;
    private final String name;
    private final Category category;
    
    public static TechStack create(String name, Category category) {
        validateName(name);
        validateCategory(category);
        return new TechStack(null, name, category);
    }
    
    @JsonCreator
    public static TechStack withId(@JsonProperty("id") Long id, 
                                   @JsonProperty("name") String name, 
                                   @JsonProperty("category") Category category) {
        validateName(name);
        validateCategory(category);
        return new TechStack(id, name, category);
    }
    
    private static void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("TechStack name cannot be empty");
        }
        if (name.length() > 50) {
            throw new IllegalArgumentException("TechStack name cannot exceed 50 characters");
        }
    }
    
    private static void validateCategory(Category category) {
        if (category == null) {
            throw new IllegalArgumentException("TechStack category cannot be null");
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TechStack techStack = (TechStack) o;
        return Objects.equals(name, techStack.name) && category == techStack.category;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(name, category);
    }
}