package com.np.redisson.test;

import com.np.redisson.test.dto.Student;
import org.junit.jupiter.api.Test;
import org.redisson.api.RMapReactive;
import org.redisson.client.codec.StringCodec;
import org.redisson.codec.TypedJsonJacksonCodec;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Map;

public class Lec06MapTest extends BaseTest {

    @Test
    public void mapTest() {
        RMapReactive<String, String> map = this.client.getMap("user:1", StringCodec.INSTANCE);
        Mono<String> name = map.put("name", "Sam");
        Mono<String> age = map.put("age", "10");
        Mono<String> city = map.put("city", "Atlanta");

        StepVerifier.create(name.concatWith(age).concatWith(city).then())
                .verifyComplete();
    }

    @Test
    public void mapTest2() {
        RMapReactive<String, String> map = this.client.getMap("user:2", StringCodec.INSTANCE);
        Map<String, String> userMap = Map.of("name", "Sam", "age", "10", "city", "Atlanta");

        StepVerifier.create(map.putAll(userMap).then())
                .verifyComplete();
    }

    @Test
    public void mapTest3() {
        // Map<Integer, String>
        TypedJsonJacksonCodec codec = new TypedJsonJacksonCodec(Integer.class, Student.class);
        RMapReactive<Integer, Student> map = this.client.getMap("user:3", codec);

        Student student1 = new Student("Sam", 10, "Atlanta", List.of(1, 2, 3));
        Student student2 = new Student("Mike", 12, "Miami", List.of(10, 20, 30));

        Mono<Student> mono1 = map.put(1, student1);
        Mono<Student> mono2 = map.put(2, student2);

        StepVerifier.create(mono1.concatWith(mono2).then())
                .verifyComplete();
    }
}
