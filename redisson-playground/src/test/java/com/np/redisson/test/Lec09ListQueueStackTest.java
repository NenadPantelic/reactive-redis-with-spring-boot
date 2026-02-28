package com.np.redisson.test;

import org.junit.jupiter.api.Test;
import org.redisson.api.RDequeReactive;
import org.redisson.api.RListReactive;
import org.redisson.api.RQueueReactive;
import org.redisson.client.codec.LongCodec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

public class Lec09ListQueueStackTest extends BaseTest {

    @Test
    public void listTest() {
        // lrange numbers 0 -1 -> get the list content in Redis
        // they might not be in the same order as you want; the reactive way of insertion
        // is not sequential, so the order is not guaranteed
        RListReactive<Long> list = this.client.getList("numbers", LongCodec.INSTANCE);
        Mono<Void> add = Flux.range(1, 10)
                .map(Long::valueOf)
                .flatMap(list::add)
                .then();

        StepVerifier.create(add)
                .verifyComplete();

        StepVerifier.create(list.size())
                .expectNext(10)
                .verifyComplete();

        List<Long> longList = LongStream.rangeClosed(1, 10)
                .boxed()
                .collect(Collectors.toList());
        // added all at once; the order is preserved
        StepVerifier.create(list.addAll(longList).then())
                .verifyComplete();
    }

    @Test
    public void queueTest() {
        RQueueReactive<Long> queue = this.client.getQueue("numbers", LongCodec.INSTANCE);

        Mono<Void> queuePoll = queue.poll() // remove the first element of the queue (from the beginning)
                .repeat(3) // do this for 3 more times once the initial command is complete
                .doOnNext(System.out::println) // 1, 2, 3, 4
                .then();

        StepVerifier.create(queuePoll)
                .verifyComplete();

        StepVerifier.create(queue.size())
                .expectNext(6)
                .verifyComplete();
    }

    @Test
    public void stackTest() { // stack is not very efficient, so it is recommended to use deque (double ended queue)
        // deque can behave like a queue and a stack
        RDequeReactive<Long> queue = this.client.getDeque("numbers", LongCodec.INSTANCE);

        Mono<Void> dequePoll = queue.pollLast() // remove the first element of the queue (from the beginning)
                .repeat(3) // do this for 3 more times once the initial command is complete
                .doOnNext(System.out::println) // 10, 9, 8, 7
                .then();

        StepVerifier.create(dequePoll)
                .verifyComplete();

        StepVerifier.create(queue.size())
                .expectNext(2)
                .verifyComplete();
    }
}
