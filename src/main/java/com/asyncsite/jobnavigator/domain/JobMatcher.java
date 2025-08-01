package com.asyncsite.jobnavigator.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.*;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class JobMatcher {
    
    @Getter
    @AllArgsConstructor
    public static class MatchResult {
        private final Job job;
        private final double matchScore;
        private final Set<TechStack> matchedRequired;
        private final Set<TechStack> matchedPreferred;
        private final Set<TechStack> missingRequired;
        private final Set<TechStack> missingPreferred;
        
        public boolean hasAllRequiredSkills() {
            return missingRequired.isEmpty();
        }
        
        public int getTotalMatchedSkills() {
            return matchedRequired.size() + matchedPreferred.size();
        }
    }
    
    private final Set<TechStack> userTechStacks;
    
    public static JobMatcher forUser(Set<TechStack> userTechStacks) {
        if (userTechStacks == null) {
            throw new IllegalArgumentException("User tech stacks cannot be null");
        }
        return new JobMatcher(new HashSet<>(userTechStacks));
    }
    
    public MatchResult match(Job job) {
        if (job == null) {
            throw new IllegalArgumentException("Job cannot be null");
        }
        
        Set<TechStack> matchedRequired = job.getRequiredTechStacks().stream()
                .filter(userTechStacks::contains)
                .collect(Collectors.toSet());
        
        Set<TechStack> matchedPreferred = job.getPreferredTechStacks().stream()
                .filter(userTechStacks::contains)
                .collect(Collectors.toSet());
        
        Set<TechStack> missingRequired = job.getRequiredTechStacks().stream()
                .filter(tech -> !userTechStacks.contains(tech))
                .collect(Collectors.toSet());
        
        Set<TechStack> missingPreferred = job.getPreferredTechStacks().stream()
                .filter(tech -> !userTechStacks.contains(tech))
                .collect(Collectors.toSet());
        
        double matchScore = job.calculateMatchScore(userTechStacks);
        
        return new MatchResult(
                job,
                matchScore,
                matchedRequired,
                matchedPreferred,
                missingRequired,
                missingPreferred
        );
    }
    
    public List<MatchResult> matchJobs(List<Job> jobs) {
        if (jobs == null) {
            return Collections.emptyList();
        }
        
        return jobs.stream()
                .filter(Job::isActive)
                .filter(job -> !job.isExpired())
                .map(this::match)
                .sorted(Comparator.comparingDouble(MatchResult::getMatchScore).reversed())
                .collect(Collectors.toList());
    }
    
    public List<MatchResult> matchJobsWithMinScore(List<Job> jobs, double minScore) {
        if (minScore < 0 || minScore > 1) {
            throw new IllegalArgumentException("Minimum score must be between 0 and 1");
        }
        
        return matchJobs(jobs).stream()
                .filter(result -> result.getMatchScore() >= minScore)
                .collect(Collectors.toList());
    }
    
    public Map<TechStack, Integer> analyzeMissingSkills(List<Job> jobs) {
        Map<TechStack, Integer> missingSkillsCount = new HashMap<>();
        
        List<MatchResult> results = matchJobs(jobs);
        
        for (MatchResult result : results) {
            for (TechStack missing : result.getMissingRequired()) {
                missingSkillsCount.merge(missing, 1, Integer::sum);
            }
            for (TechStack missing : result.getMissingPreferred()) {
                missingSkillsCount.merge(missing, 1, Integer::sum);
            }
        }
        
        return missingSkillsCount.entrySet().stream()
                .sorted(Map.Entry.<TechStack, Integer>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }
}