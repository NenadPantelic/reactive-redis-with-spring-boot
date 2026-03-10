package com.np.redisson.test;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.redisson.api.RBucketReactive;
import org.redisson.api.RTransaction;
import org.redisson.api.RTransactionReactive;
import org.redisson.api.TransactionOptions;
import org.redisson.client.codec.LongCodec;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class Lec14TransactionTest extends BaseTest {

    private RBucketReactive<Long> userBalance1;
    private RBucketReactive<Long> userBalance2;

    // user:1:balance 100
    // user:2:balance 0
    @BeforeAll
    public void accountSetup() {
        this.userBalance1 = this.client.getBucket("user:1:balance", LongCodec.INSTANCE);
        this.userBalance2 = this.client.getBucket("user:2:balance", LongCodec.INSTANCE);

        Mono<Void> mono = userBalance1.set(100L)
                .then(userBalance2.set(0L))
                .then();

        StepVerifier.create(mono)
                .verifyComplete();
    }

    @AfterAll
    public void accountBalanceStatus() {
        Mono<Void> mono = Flux.zip(this.userBalance1.get(), this.userBalance2.get())
                .doOnNext(System.out::println)
                .then();

        StepVerifier.create(mono)
                .verifyComplete();
    }


    @Test
    public void nonTransactionTest() {
        RBucketReactive<Long> userBalance1 = this.client.getBucket("user:1:balance", LongCodec.INSTANCE);
        RBucketReactive<Long> userBalance2 = this.client.getBucket("user:2:balance", LongCodec.INSTANCE);

        this.transfer(userBalance1, userBalance2, 50)
                .thenReturn(0)
                .map(i -> 5 / i) // will raise ArithmeticException: division by zero
                .doOnError(System.out::println)
                .subscribe();

        sleep(1000);
    }

    @Test
    public void transactionTest() {
        // the amount transfer between accounts will happen within a transaction
        RTransactionReactive transaction = this.client.createTransaction(TransactionOptions.defaults());
        RBucketReactive<Long> userBalance1 = transaction.getBucket("user:1:balance", LongCodec.INSTANCE);
        RBucketReactive<Long> userBalance2 = transaction.getBucket("user:2:balance", LongCodec.INSTANCE);

        this.transfer(userBalance1, userBalance2, 50)
                .thenReturn(0)
                .map(i -> 5 / i) // will raise ArithmeticException: division by zero
                .then(transaction.commit())
                .onErrorResume(ex -> transaction.rollback())
                .subscribe();

        sleep(1000);
    }

    private Mono<Void> transfer(RBucketReactive<Long> from, RBucketReactive<Long> to, int amount) {
        return Flux.zip(from.get(), to.get())
                .filter(t -> t.getT1() >= amount)
                .flatMap(t -> from.set(t.getT1() - amount).thenReturn(t))
                .flatMap(t -> to.set(t.getT2() + amount))
                .then();
    }


}
