package com.asyncsite.jobnavigator.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Swagger/OpenAPI 설정
 */
@OpenAPIDefinition(
    info = @Info(
        title = "Job Navigator Service API",
        description = "API for managing job postings, companies, and tech stacks",
        version = "v1.0.0",
        contact = @Contact(
            name = "AsyncSite Job Navigator Team",
            email = "job-navigator@asyncsite.com"
        ),
        license = @License(
            name = "Apache 2.0",
            url = "https://www.apache.org/licenses/LICENSE-2.0"
        )
    )
)
@Configuration
public class SwaggerConfig {

    @Value("${server.port:12085}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        // 서버 목록 설정
        List<Server> servers = new ArrayList<>();
        
        // Gateway를 통한 접근
        Server gatewayServer = new Server()
                .url("http://localhost:8080/api/jobs")
                .description("Gateway Server (Development)");
        servers.add(gatewayServer);
        
        // 직접 접근
        Server directServer = new Server()
                .url("http://localhost:" + serverPort)
                .description("Direct Server (Development)");
        servers.add(directServer);
        
        // Production Gateway
        Server prodGatewayServer = new Server()
                .url("https://api.asyncsite.com/api/jobs")
                .description("Gateway Server (Production)");
        servers.add(prodGatewayServer);
        
        // Security 설정
        SecurityScheme bearerAuth = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("JWT token authentication.\n\n" +
                    "**Usage:**\n" +
                    "1. Login via /api/auth/login endpoint\n" +
                    "2. Copy the accessToken from response\n" +
                    "3. Click 'Authorize' and enter the token\n\n" +
                    "**Note:** The 'Bearer ' prefix is added automatically.");
        
        return new OpenAPI()
                .servers(servers)
                .addSecurityItem(new SecurityRequirement().addList("JWT Authentication"))
                .components(
                    new Components()
                        .addSecuritySchemes("JWT Authentication", bearerAuth)
                );
    }

    /**
     * 모든 API를 포함하는 기본 그룹
     */
    @Bean
    public GroupedOpenApi allApi() {
        return GroupedOpenApi.builder()
                .group("all-apis")
                .pathsToMatch("/**")
                .build();
    }
    
    /**
     * Public API 그룹 - 일반 사용자용 API
     */
    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("public-api")
                .pathsToMatch("/api/jobs/**", "/api/companies/**", "/api/tech-stacks/**")
                .pathsToExclude("/api/jobs/batch", "/api/admin/**")
                .build();
    }

    /**
     * Internal API 그룹 - 크롤러 서비스용 API
     */
    @Bean
    public GroupedOpenApi internalApi() {
        return GroupedOpenApi.builder()
                .group("internal-api")
                .pathsToMatch("/api/jobs/batch", "/api/crawl/**")
                .build();
    }

    /**
     * Admin API 그룹 - 관리자용 API
     */
    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
                .group("admin-api")
                .pathsToMatch("/api/admin/**")
                .build();
    }
}