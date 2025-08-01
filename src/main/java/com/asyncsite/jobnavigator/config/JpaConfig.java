package com.asyncsite.jobnavigator.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "com.asyncsite.jobnavigator.adapter.out.persistence.repository")
@EnableJpaAuditing
public class JpaConfig {
}