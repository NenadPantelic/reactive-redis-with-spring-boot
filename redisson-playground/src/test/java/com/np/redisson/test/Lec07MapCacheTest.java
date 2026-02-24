package com.np.redisson.test;

import com.np.redisson.test.dto.Student;
import org.junit.jupiter.api.Test;
import org.redisson.api.RMapCacheReactive;
import org.redisson.codec.TypedJsonJacksonCodec;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class Lec07MapCacheTest extends BaseTest {

    @Test
    public void mapCacheTest() {
        // Map<Integer, String>
        TypedJsonJacksonCodec codec = new TypedJsonJacksonCodec(Integer.class, Student.class);
        RMapCacheReactive<Integer, Student> mapCache = this.client.getMapCache("user:3", codec);

        Student student1 = new Student("Sam", 10, "Atlanta", List.of(1, 2, 3));
        Student student2 = new Student("Mike", 12, "Miami", List.of(10, 20, 30));

        Mono<Student> s1 = mapCache.put(1, student1, 5, TimeUnit.SECONDS);
        Mono<Student> s2 = mapCache.put(2, student2, 10, TimeUnit.SECONDS);

        StepVerifier.create(s1.concatWith(s2).then())
                .verifyComplete();

        sleep(3000);

        // access students
        mapCache.get(1).doOnNext(System.out::println).subscribe();
        mapCache.get(2).doOnNext(System.out::println).subscribe();

        sleep(3000);

        // access students
        mapCache.get(1).doOnNext(System.out::println).subscribe(); // this one should have been evicted by now
        mapCache.get(2).doOnNext(System.out::println).subscribe();
    }
}
