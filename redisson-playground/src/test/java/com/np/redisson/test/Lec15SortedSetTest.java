package com.np.redisson.test;

import org.junit.jupiter.api.Test;
import org.redisson.api.RScoredSortedSetReactive;
import org.redisson.client.codec.StringCodec;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.function.Function;

public class Lec15SortedSetTest extends BaseTest {

    @Test
    public void sortedSet() {
        RScoredSortedSetReactive<Object> sortedSet = this.client.getScoredSortedSet("student:score", StringCodec.INSTANCE);

        // entries are sorted ascending by their score
        Mono<Void> mono = sortedSet.addScore("sam", 12.25) // increases the score every time it is executed
                .then(sortedSet.add(23.25, "mike")) // sets the fixed score
                .then(sortedSet.addScore("jake", 7))
                .then();

        StepVerifier.create(mono)
                .verifyComplete();

        sortedSet.entryRange(0, 1) // ranks between 0 and 1 - rank is their index when they are sorted ascending
                .flatMapIterable(Function.identity())
                .map(se -> se.getScore() + ": " + se.getValue())
                .doOnNext(System.out::println)
                .subscribe();

        sleep(1000);
    }
}
