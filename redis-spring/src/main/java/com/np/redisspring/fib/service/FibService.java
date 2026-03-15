package com.np.redisspring.fib.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FibService {

    // @Cacheable("math:fib") // bucket name - hash in Redis; it uses all the parameters of the function by default
    // It uses them as a hash key.

    // GET - caches results
    // POST, PUT, DELETE - evicts results (the result is updated)
    @Cacheable(value = "math:fib", key = "#index") // it explicitly uses just "index" param as key
    public long getFib(int index, String name) {
        log.info("Calculating fib for index = {} and name = {} ", index, name);
        return this.fib(index);
    }

    // executed on eviction
    // not reactive which is fine for primitive types
    @CacheEvict(value = "math:fib", key = "#index")
    public void clearCache(int index) {
        log.info("Clearing cache key {} ", index);
    }

    @Scheduled(fixedRate = 5000) // 5s
    @CacheEvict(value = "math:fib", allEntries = true)
    public void clearAllCache() {
        log.info("Clearing all fib keys");
    }


    // 0, 1, 1, 2, 3, 5, 8, 13...
    // intentionally 2 ^ n
    private long fib(int index) {
        if (index < 2) {
            return index;
        }

        return fib(index - 1) + fib(index - 2);
    }
}
