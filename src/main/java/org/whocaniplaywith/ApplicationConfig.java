package org.whocaniplaywith;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
@EnableCaching
@EnableScheduling
@EnableAutoConfiguration(exclude={DataSourceAutoConfiguration.class}) // Remove if using a DB
public class ApplicationConfig {
    @Bean
    public Executor taskExecutor(
        @Value("${spring.task.execution.pool.core-size}") int corePoolSize,
        @Value("${spring.task.execution.pool.max-size}") int maxPoolSize,
        @Value("${spring.task.execution.pool.queue-capacity}") int queueCapacity,
        @Value("${spring.task.execution.thread-name-prefix}") String threadNamePrefix
    ) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix(threadNamePrefix);
        executor.initialize();

        return executor;
    }
}
