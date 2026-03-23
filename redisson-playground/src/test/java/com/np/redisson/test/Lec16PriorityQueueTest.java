package com.np.redisson.test;

import com.np.redisson.test.assignment.Category;
import com.np.redisson.test.assignment.PriorityQueue;
import com.np.redisson.test.assignment.UserOrder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.redisson.api.RScoredSortedSetReactive;
import org.redisson.codec.TypedJsonJacksonCodec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;

public class Lec16PriorityQueueTest extends BaseTest {

    private PriorityQueue priorityQueue;

    @BeforeAll
    public void setupQueue() {
        RScoredSortedSetReactive<UserOrder> sortedSet = this.client.getScoredSortedSet(
                "user:order:queue",
                new TypedJsonJacksonCodec(UserOrder.class)
        );

        this.priorityQueue = new PriorityQueue(sortedSet);
    }

    @Test
    public void producer() {
        UserOrder userOrder1 = new UserOrder(1, Category.GUEST);
        UserOrder userOrder2 = new UserOrder(2, Category.STD);
        UserOrder userOrder3 = new UserOrder(3, Category.PRIME);
        UserOrder userOrder4 = new UserOrder(4, Category.STD);
        UserOrder userOrder5 = new UserOrder(5, Category.GUEST);

        Mono<Void> mono = Flux.just(userOrder1, userOrder2, userOrder3, userOrder4, userOrder5)
                .flatMap(this.priorityQueue::add)
                .then();

        StepVerifier.create(mono)
                .verifyComplete();
    }

    @Test
    public void producer2() {
        Flux.interval(Duration.ofSeconds(1))
                .map(l -> (l.intValue() * 5))
                .doOnNext(i -> {
                    UserOrder userOrder1 = new UserOrder(i + 1, Category.GUEST);
                    UserOrder userOrder2 = new UserOrder(i + 2, Category.STD);
                    UserOrder userOrder3 = new UserOrder(i + 3, Category.PRIME);
                    UserOrder userOrder4 = new UserOrder(i + 4, Category.STD);
                    UserOrder userOrder5 = new UserOrder(i + 5, Category.GUEST);

                    Mono<Void> mono = Flux.just(userOrder1, userOrder2, userOrder3, userOrder4, userOrder5)
                            .flatMap(this.priorityQueue::add)
                            .then();

                    StepVerifier.create(mono)
                            .verifyComplete();
                });

        sleep(60_000);
    }

    @Test
    public void consumer() {
        this.priorityQueue.takeItems()
                .delayElements(Duration.ofMillis(500)) // consumer is slow
                .doOnNext(System.out::println)
                .subscribe();

        sleep(600_000);
    }
}
