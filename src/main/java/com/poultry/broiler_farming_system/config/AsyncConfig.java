package com.poultry.broiler_farming_system.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

// Backs @Async SmsRequestedEventListener. A dedicated bounded pool, not
// Spring's default SimpleAsyncTaskExecutor (unbounded thread-per-task) --
// an SMS gateway outage or a large daily-log-enforcement scan shouldn't be
// able to spawn an unbounded number of threads.
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "notificationExecutor")
    public TaskExecutor notificationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("notification-async-");
        executor.initialize();
        return executor;
    }
}
