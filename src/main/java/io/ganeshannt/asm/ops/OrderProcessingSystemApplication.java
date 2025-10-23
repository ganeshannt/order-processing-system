package io.ganeshannt.asm.ops;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * @author Ganeshannt
 * @version 1.0
 * @since 2025-10-22
 */
@SpringBootApplication
@EnableAsync
public class OrderProcessingSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderProcessingSystemApplication.class, args);
    }
}
