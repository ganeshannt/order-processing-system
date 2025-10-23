package io.ganeshannt.asm.ops.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @author Ganeshannt
 * @version 1.0
 * @since 2025-10-23
 */
@Configuration
public class OpenAPIConfig {

    @Value("${spring.application.name:Order Processing System}")
    private String applicationName;

    /**
     * Configure OpenAPI documentation
     * <p>
     * Includes:
     * - API metadata (title, version, description)
     * - Contact information
     * - License
     * - Server URLs
     * - Security schemes (for future use)
     *
     * @return OpenAPI configuration
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(apiServers())
                .components(securityComponents());
    }

    /**
     * API Information
     * <p>
     * Displayed at the top of Swagger UI
     */
    private Info apiInfo() {
        return new Info()
                .title("Order Processing System API")
                .version("1.0.0")
                .description("""
                        # E-commerce Order Processing System REST API
                        
                        ## Overview
                        Production-grade REST API for managing e-commerce orders with automated workflows.
                        
                        ## Features
                        - ✅ Create orders with multiple items
                        - ✅ Retrieve order details and history
                        - ✅ Automatic order status updates (PENDING → PROCESSING every 5 minutes)
                        - ✅ Cancel orders (PENDING only)
                        - ✅ Order statistics and analytics
                        - ✅ Comprehensive validation and error handling
                        
                        ## Technology Stack
                        - Java 21 (Virtual Threads)
                        - Spring Boot 3.5.6
                        - Spring Data JPA
                        - H2 Database (in-memory)
                        - MapStruct (DTO mapping)
                        - OpenAPI 3.0
                        
                        ## Order Status Lifecycle
                        ```
                        PENDING → PROCESSING → SHIPPED → DELIVERED
                           ↓
                        CANCELLED (only from PENDING)
                        ```
                        
                        ## Getting Started
                        1. Use the interactive API explorer below
                        2. Click "Try it out" on any endpoint
                        3. Fill in required parameters
                        4. Click "Execute" to test the API
                        
                        ## Support
                        For issues or questions, contact the development team.
                        """)
                .contact(new Contact()
                        .name("Ganeshannt")
                        .email("ganeshannt@example.com")
                        .url("https://github.com/ganeshannt"))
                .license(new License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT"));
    }

    /**
     * API Servers
     * <p>
     * Multiple server configurations for different environments
     * User can switch between servers in Swagger UI
     */
    private List<Server> apiServers() {
        return List.of(
                new Server()
                        .url("http://localhost:8080")
                        .description("Local Development Server"),
                new Server()
                        .url("https://api-staging.example.com")
                        .description("Staging Server"),
                new Server()
                        .url("https://api.example.com")
                        .description("Production Server")
        );
    }

    /**
     * Security Components
     * <p>
     * Defines security schemes for API
     * Currently configured for JWT Bearer token (for future implementation)
     * <p>
     * When security is implemented, endpoints will show "Authorize" button
     */
    private Components securityComponents() {
        return new Components()
                .addSecuritySchemes("bearerAuth",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT Bearer token authentication. " +
                                        "Format: Bearer {token}"));
    }
}
