package io.ganeshannt.asm.ops.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @author Ganeshannt
 * @version 1.1
 * @since 2025-10-23
 */
@Configuration
public class OpenAPIConfig {

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${server.port:8080}")
    private String serverPort;

    private static final String BUILD_DESCRIPTION = """
            Production-grade REST API for managing e-commerce orders with automated workflows.
            
            ## Key Features
            - Create orders with multiple items
            - Retrieve order details with pagination (1-indexed)
            - Automatic status updates (PENDING → PROCESSING every 5 minutes)
            - Cancel PENDING orders
            - Comprehensive validation and error handling
            
            ## Order Status Lifecycle
            ```
            PENDING → PROCESSING → SHIPPED → DELIVERED
               ↓
            CANCELLED (only from PENDING)
            ```
            
            ## Technology Stack
            - **Language:** Java 21
            - **Framework:** Spring Boot 3.5.6
            - **Database:** H2 (in-memory)
            - **Documentation:** OpenAPI 3.0
            
            ## Getting Started
            1. Explore endpoints using the interactive API explorer below
            2. Click **"Try it out"** on any endpoint
            3. Fill in required parameters
            4. Click **"Execute"** to test the API
            """;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(buildApiInfo())
                .servers(buildServerList());
    }

    private Info buildApiInfo() {
        return new Info()
                .title(applicationName + " API")
                .version("1.0.0")
                .description(BUILD_DESCRIPTION)
                .contact(buildContact())
                .license(buildLicense());
    }

    private Contact buildContact() {
        return new Contact()
                .name("Ganeshannt")
                .email("ganeshannt@example.com")
                .url("https://github.com/ganeshannt");
    }


    private License buildLicense() {
        return new License()
                .name("MIT License")
                .url("https://opensource.org/licenses/MIT");
    }

    private List<Server> buildServerList() {
        return List.of(
                new Server()
                        .url("http://localhost:" + serverPort)
                        .description("Local Development Server")
        );
    }
}
