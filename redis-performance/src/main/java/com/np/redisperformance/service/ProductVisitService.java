package com.np.redisperformance.service;

import jakarta.annotation.PostConstruct;
import org.redisson.api.BatchOptions;
import org.redisson.api.RBatchReactive;
import org.redisson.api.RScoredSortedSetReactive;
import org.redisson.api.RedissonReactiveClient;
import org.redisson.client.codec.IntegerCodec;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ProductVisitService {

    private final RedissonReactiveClient client;
    private final Sinks.Many<Integer> sink;

    public ProductVisitService(RedissonReactiveClient client) {
        this.client = client;
        // 1. Sinks.many(): This indicates the creation of a sink that can emit multiple elements, potentially over a long
        // period, which will result in a Flux when consumed as a publisher.
        //2. unicast(): This ensures that the resulting Flux can only have a single Subscriber at any given time.
        // If a second subscriber attempts to subscribe, the subscription will fail with an error. This is different
        // from a multicast sink which allows multiple subscribers.
        // 3. onBackpressureBuffer(): This configures the sink's backpressure handling strategy. It uses an internal
        // queue to buffer all signals (elements, completion, or error) until the sole subscriber requests them.
        // By default, this uses an unbounded buffer.
        this.sink = Sinks.many().unicast().onBackpressureBuffer();
    }

    @PostConstruct
    private void init() {
        // subscriber
        this.sink.asFlux()
                // every 3 seconds
                .buffer(Duration.ofSeconds(3)) // list(1,2,1,1,3,5,1...)
                .map(l -> l.stream().collect(
                                Collectors.groupingBy( // 1:3. 5:1,..
                                        Function.identity(),
                                        Collectors.counting()
                                )
                        )
                )
                .flatMap(this::updateBatch);
    }

    public void addVisit(int productId) {
        this.sink.tryEmitNext(productId);
    }

    private Mono<Void> updateBatch(Map<Integer, Long> map) {
        RBatchReactive batch = this.client.createBatch(BatchOptions.defaults());
        String format = DateTimeFormatter.ofPattern("YYYYMMdd").format(LocalDate.now());
        RScoredSortedSetReactive<Integer> sortedSet = batch.getScoredSortedSet(
                "product:visit:" + format,
                IntegerCodec.INSTANCE
        );
        return Flux.fromIterable(map.entrySet())
                .map(e -> sortedSet.addScore(e.getKey(), e.getValue()))
                .then(batch.execute())
                .then();
    }
}
