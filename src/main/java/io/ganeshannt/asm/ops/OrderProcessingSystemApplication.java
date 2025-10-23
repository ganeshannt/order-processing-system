package io.ganeshannt.asm.ops;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main Application Entry Point
 *
 * Architecture Decision:
 * - @EnableScheduling: Enables background scheduled tasks (order status updates)
 * - @EnableAsync: Enables asynchronous method execution for non-blocking operations
 * - Virtual threads are enabled via application.properties for better concurrency
 *
 * @author Ganeshannt
 * @version 1.0
 * @since 2025-10-22
 */
@SpringBootApplication
@EnableScheduling
@EnableAsync
public class OrderProcessingSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderProcessingSystemApplication.class, args);
    }
}
