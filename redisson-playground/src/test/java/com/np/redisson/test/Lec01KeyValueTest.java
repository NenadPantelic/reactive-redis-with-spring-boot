package com.np.redisson.test;

import org.junit.jupiter.api.Test;
import org.redisson.api.RBucketReactive;
import org.redisson.client.codec.StringCodec;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;

public class Lec01KeyValueTest extends BaseTest {

    @Test
    public void keyValueAccessTest() {
        // get object holder by key
        RBucketReactive<String> bucket = this.client.getBucket("user:1:name");
        // set value associated with given key
        Mono<Void> set = bucket.set("Nenad");

        Mono<Void> get = bucket.get().doOnNext(System.out::println).then();

        // first do the "set"
        // once it is done, do the "get"
        StepVerifier.create(set.concatWith(get)).verifyComplete();
    }

    @Test
    public void keyValueAccessWithCodedTest() {
        // to serialize strings
        RBucketReactive<String> bucket = this.client.getBucket("user:1:name:serialized", StringCodec.INSTANCE);
        // set value associated with given key
        Mono<Void> set = bucket.set("Nenad");

        Mono<Void> get = bucket.get().doOnNext(System.out::println).then();

        StepVerifier.create(set.concatWith(get)).verifyComplete();
    }

    @Test
    public void keyValueExpiryTest() {
        RBucketReactive<String> bucket = this.client.getBucket("user:1:name:expiry", StringCodec.INSTANCE);
        // set value associated with given key with expiry
        Mono<Void> set = bucket.set("Nenad", Duration.ofSeconds(10));
        Mono<Void> get = bucket.get().doOnNext(System.out::println).then();
        StepVerifier.create(set.concatWith(get)).verifyComplete();
    }

    @Test
    public void keyValueExtendExpiryTest() {
        RBucketReactive<String> bucket = this.client.getBucket("user:1:name:extendexpiry", StringCodec.INSTANCE);
        // set value associated with given key with expiry
        Mono<Void> set = bucket.set("Nenad", Duration.ofSeconds(10));
        Mono<Void> get = bucket.get().doOnNext(System.out::println).then();
        StepVerifier.create(set.concatWith(get)).verifyComplete();

        // extend the expiry
        sleep(5000); // sleep for 5s
        Mono<Boolean> mono = bucket.expire(Duration.ofSeconds(60));
        StepVerifier.create(mono).expectNext(true).verifyComplete();

        // access expiration time
        Mono<Void> ttl = bucket.remainTimeToLive().doOnNext(System.out::println)
                .then();

        StepVerifier.create(ttl).verifyComplete();
    }
}
