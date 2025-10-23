package io.ganeshannt.asm.ops.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.VirtualThreadTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * Configures virtual threads for concurrent operations and scheduling.
 *
 * Virtual threads provide lightweight concurrency, ideal for I/O-heavy tasks.
 */
@Configuration
public class VirtualThreadConfig {

    @Bean
    public VirtualThreadTaskExecutor virtualThreadTaskExecutor() {
        return new VirtualThreadTaskExecutor("ops-vt-");
    }

    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        var scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(2);
        scheduler.setThreadNamePrefix("ops-scheduler-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(10);
        return scheduler;
    }
}
