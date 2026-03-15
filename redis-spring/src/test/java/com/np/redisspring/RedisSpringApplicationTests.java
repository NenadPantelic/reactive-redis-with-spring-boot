package com.np.redisspring;

import org.junit.jupiter.api.RepeatedTest;
import org.redisson.api.RAtomicLongReactive;
import org.redisson.api.RedissonReactiveClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringBootTest
class RedisSpringApplicationTests {

    @Autowired
    private ReactiveStringRedisTemplate template; // Spring Data

    @Autowired
    private RedissonReactiveClient client; // Redisson

    // for more accurate benchmarking; the first run will warm up the JVM
    @RepeatedTest(3)
    void springDataRedisTest() {
        // Redisson does not directly use Spring Data Redis internally for its core operations,
        // but it provides a compatibility layer and integration module that implements the Spring Data Redis API.
        // On benchmarking - Redisson gave slightly better results for this test
        ReactiveValueOperations<String, String> valueOperations = this.template.opsForValue();

        long before = System.currentTimeMillis();
        Mono<Void> mono = Flux.range(1, 500_000)
                // 500_000 network calls
                .flatMap(i -> valueOperations.increment("user:1:visit")) // incr
                .then();

        StepVerifier.create(mono)
                .verifyComplete();
        long after = System.currentTimeMillis();

        System.out.println((after - before) + " ms");
    }

    // better performance than Spring Data
    @RepeatedTest(3)
    void redissonTest() {
        RAtomicLongReactive atomicLong = this.client.getAtomicLong("user:2:visit");

        long before = System.currentTimeMillis();
        Mono<Void> mono = Flux.range(1, 500_000)
                // 500_000 network calls
                .flatMap(i -> atomicLong.incrementAndGet()) // incr
                .then();

        StepVerifier.create(mono)
                .verifyComplete();
        long after = System.currentTimeMillis();

        System.out.println((after - before) + " ms");
    }

    // Flaws of Spring Data:
    // 1. Performance issues
    // 2. No support for Reactive CRUD repository
    // 3. Some annotations do not work with reactive type
}
