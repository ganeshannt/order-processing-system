package io.ganeshannt.asm.ops.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * @author Ganeshannt
 * @version 1.0
 * @since 2025-10-23
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ApplicationStartupListener {

    private final Environment environment;

    @Value("${spring.datasource.url:N/A}")
    private String datasourceUrl;

    @Value("${spring.jpa.hibernate.ddl-auto:N/A}")
    private String ddlAuto;

    @Value("${spring.h2.console.enabled:false}")
    private boolean h2ConsoleEnabled;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        String[] activeProfiles = environment.getActiveProfiles();
        String profileInfo = activeProfiles.length > 0
                ? String.join(", ", activeProfiles)
                : "default";

        log.info("=".repeat(80));
        log.info("APPLICATION STARTED SUCCESSFULLY");
        log.info("=".repeat(80));
        log.info("Application Name    : {}", environment.getProperty("spring.application.name"));
        log.info("Active Profile(s)   : {}", profileInfo);
        log.info("Server Port         : {}", environment.getProperty("server.port"));
        log.info("Java Version        : {}", System.getProperty("java.version"));
        log.info("Virtual Threads     : {}", environment.getProperty("spring.threads.virtual.enabled"));
        log.info("-".repeat(80));
        log.info("Database URL        : {}", datasourceUrl);
        log.info("Hibernate DDL Auto  : {}", ddlAuto);
        log.info("H2 Console Enabled  : {}", h2ConsoleEnabled);

        if (h2ConsoleEnabled) {
            log.info("H2 Console          : http://localhost:{}/h2-console",
                    environment.getProperty("server.port"));
        }

        log.info("-".repeat(80));
        log.info("Swagger UI          : http://localhost:{}/swagger-ui.html",
                environment.getProperty("server.port"));
        log.info("OpenAPI Docs        : http://localhost:{}/api-docs",
                environment.getProperty("server.port"));
        log.info("Actuator Health     : http://localhost:{}/actuator/health",
                environment.getProperty("server.port"));
        log.info("=".repeat(80));

        // Profile-specific messages
        if (Arrays.asList(activeProfiles).contains("dev")) {
            log.info("DEVELOPMENT MODE");
            log.info("   - Sample data has been loaded");
            log.info("   - Database will be recreated on restart");
            log.info("   - SQL logging is enabled");
        } else if (Arrays.asList(activeProfiles).contains("prod")) {
            log.info("🔒 PRODUCTION MODE");
            log.info("   - H2 console is disabled");
            log.info("   - Database schema will be updated only");
            log.info("   - No sample data loaded");
        }

        log.info("=".repeat(80));
    }
}
