package com.np.redisson.test;

import com.np.redisson.test.dto.Student;
import org.junit.jupiter.api.Test;
import org.redisson.api.RBucketReactive;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.codec.TypedJsonJacksonCodec;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

public class Lec02KeyValueObjectTest extends BaseTest {

    @Test
    public void keyValueObjectTest() {
        Student student = new Student("marshall", 10, "Atlanta", List.of(1, 2, 3));
        // JBoss Marshalling is the default one, it expects the class to implement the Serializable interface
        // use Jackson codec to store it as JSON
        // that JSON also contain the @class key which represents the full name of the class, so it knows how to
        // deserialize it
//        RBucketReactive<Student> bucket = this.client.getBucket("student:1", JsonJacksonCodec.INSTANCE);

        // To remove the class info from JSON
        RBucketReactive<Student> bucket = this.client.getBucket("student:1", new TypedJsonJacksonCodec(Student.class));

        Mono<Void> set = bucket.set(student);
        Mono<Void> get = bucket.get()
                .doOnNext(System.out::println)
                .then();

        StepVerifier.create(set.concatWith(get))
                .verifyComplete();
    }
}
