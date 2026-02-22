package com.np.redisson.test;

import org.junit.jupiter.api.Test;
import org.redisson.api.RAtomicLongReactive;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;

public class Lec03NumberTest extends BaseTest {

    @Test
    public void keyValueIncreaseTest() {
        // set k v -- incr, decr
        RAtomicLongReactive atomicLongReactive = this.client.getAtomicLong("user:1:visit");
        Mono<Void> mono = Flux.range(1, 30)
                .delayElements(Duration.ofSeconds(1))
                .flatMap(i -> atomicLongReactive.incrementAndGet())
                .then();

        StepVerifier.create(mono)
                .verifyComplete();
    }
}
