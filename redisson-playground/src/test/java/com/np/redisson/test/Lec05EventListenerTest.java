package com.np.redisson.test;

import org.junit.jupiter.api.Test;
import org.redisson.api.DeletedObjectListener;
import org.redisson.api.ExpiredObjectListener;
import org.redisson.api.RBucketReactive;
import org.redisson.client.codec.StringCodec;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;

public class Lec05EventListenerTest extends BaseTest {

    @Test
    public void expiredEventTest() {
        RBucketReactive<Object> bucket = this.client.getBucket("user:1:name", StringCodec.INSTANCE);
        Mono<Void> set = bucket.set("sam", Duration.ofSeconds(10));
        Mono<Void> get = bucket.get()
                .doOnNext(System.out::println)
                .then();

        Mono<Void> event = bucket.addListener(new ExpiredObjectListener() {
            // NOTE: this will not work out of the box, Redis will notify us
            // We have to configure that mechanism to get the notification
            //   - adjust the redis.conf file (for permanent, production config)
            //   - or via the CONFIG SET (temporary thing)
            // There are various events for which Redis can react and notify you.
            // https://redis.io/docs/latest/develop/pubsub/keyspace-notifications/
            // `config set notify-keyspace-events AKE`
            @Override
            public void onExpired(String s) {
                System.out.println("Expired: " + s);
            }
        }).then();

        StepVerifier.create(set.concatWith(get).concatWith(event))
                .verifyComplete();

        sleep(11000);
    }

    @Test
    public void deletedEventTest() {
        RBucketReactive<Object> bucket = this.client.getBucket("user:1:name", StringCodec.INSTANCE);
        Mono<Void> set = bucket.set("sam", Duration.ofSeconds(10));
        Mono<Void> get = bucket.get()
                .doOnNext(System.out::println)
                .then();

        Mono<Void> event = bucket.addListener(new DeletedObjectListener() {
            @Override
            public void onDeleted(String s) {
                System.out.println("Deleted: " + s);
            }
        }).then();

        StepVerifier.create(set.concatWith(get).concatWith(event))
                .verifyComplete();

        sleep(11000);
    }
}
