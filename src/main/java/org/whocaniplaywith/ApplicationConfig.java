package org.whocaniplaywith;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.time.LocalDateTime;
import java.util.concurrent.Executor;

@Configuration
@EnableAsync
@EnableCaching
@EnableScheduling
@Slf4j
public class ApplicationConfig {
    private static final long WEEK_IN_MILLISECONDS = 1000 * 60 * 60 * 24 * 7;
    public static final String STEAM_GAME_DETAILS_CACHE_NAME = "steamGameDetails";

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

    @Bean
    public CacheManager cacheManager() {
        log.info("Caching activated for: {}", STEAM_GAME_DETAILS_CACHE_NAME);

        return new ConcurrentMapCacheManager(STEAM_GAME_DETAILS_CACHE_NAME);
    }

    @CacheEvict(allEntries = true, value = STEAM_GAME_DETAILS_CACHE_NAME)
    @Scheduled(fixedDelay = WEEK_IN_MILLISECONDS)
    public void clearKissanimeTitleCache() {
        log.info("Cleared SteamGameDetails cache at {}", LocalDateTime.now());
    }
}
