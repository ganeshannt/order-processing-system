package io.ganeshannt.asm.ops.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Scheduler Configuration
 *
 * Only enables scheduling when property is true
 * Disabled by default in test profile
 *
 * @author Ganeshannt
 * @version 1.1
 * @since 2025-10-23
 */
@Configuration
@EnableScheduling
@ConditionalOnProperty(
        name = "order.scheduler.enabled",
        havingValue = "true",
        matchIfMissing = true  // Enabled by default unless explicitly disabled
)
public class SchedulerConfig {
    // Scheduler configuration
}
