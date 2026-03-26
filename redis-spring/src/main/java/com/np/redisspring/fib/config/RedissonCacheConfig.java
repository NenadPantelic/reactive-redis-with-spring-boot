package com.np.redisspring.fib.config;

import org.redisson.api.RedissonClient;
import org.redisson.spring.cache.RedissonSpringCacheManager;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonCacheConfig {

    @Bean
    // the default implementation for cache manager in Spring is ConcurrentHashMap
    // here we are implementing Redisson cache manager that uses Redis of course
    public CacheManager cacheManager(RedissonClient redissonClient) {
        return new RedissonSpringCacheManager(redissonClient);
    }
}
