package com.np.redisperformance.service;

import org.redisson.api.RScoredSortedSetReactive;
import org.redisson.api.RedissonReactiveClient;
import org.redisson.client.codec.IntegerCodec;
import org.redisson.client.protocol.ScoredEntry;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BusinessMetricsService {

    private final RedissonReactiveClient client;

    public BusinessMetricsService(RedissonReactiveClient client) {
        this.client = client;
    }

    public Mono<Map<Integer, Double>> topNProducts(int n) {
        String format = DateTimeFormatter.ofPattern("YYYYMMdd").format(LocalDate.now());
        RScoredSortedSetReactive<Integer> set = client.getScoredSortedSet("product:visit" + format, IntegerCodec.INSTANCE);
        return set.entryRangeReversed(0, n - 1) // list of scored entries
                // product id: view count (sorted descending)
                .map(listOfScoredEntries -> listOfScoredEntries.stream()
                        // unordered
                        .collect(Collectors.toMap(
                                        ScoredEntry::getValue,
                                        ScoredEntry::getScore,
                                        (a, b) -> a,
                                        LinkedHashMap::new
                                )
                        )
                );
    }
}
