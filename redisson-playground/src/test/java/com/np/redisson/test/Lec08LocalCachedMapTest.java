package com.np.redisson.test;

import com.np.redisson.test.config.RedissonConfig;
import com.np.redisson.test.dto.Student;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLocalCachedMap;
import org.redisson.api.RedissonClient;
import org.redisson.api.options.LocalCachedMapOptions;
import org.redisson.codec.TypedJsonJacksonCodec;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;

public class Lec08LocalCachedMapTest extends BaseTest {

    private RLocalCachedMap<Integer, Student> studentsMap;

    @BeforeAll
    public void setupClient() {
        RedissonConfig config = new RedissonConfig();
        RedissonClient redissonClient = config.getClient();

        LocalCachedMapOptions<Integer, Student> mapOptions = LocalCachedMapOptions.<Integer, Student>name("students")
                .codec(new TypedJsonJacksonCodec(Integer.class, Student.class)).timeToLive(Duration.ofSeconds(30)) // after 30s, it has to be updated/synced from the server
                // three types of sync strategy
                // 1. NONE - other clients will not be updated when Redis cache/hash is updated
                // 2. INVALIDATE - it will remove the object once it is updated on server
                // 3. UPDATE - it will immediately update all local copies in all clients (Redis will push an update)
                .syncStrategy(LocalCachedMapOptions.SyncStrategy.UPDATE)
                // Reconnection strategy
                // clients maintain the permanent TCP connection, but it can be broken
                // 4. CLEAR  - if the connection is broken, it will clear the data in the local cached map once
                // it reconnects to Redis
                // NOTE: if it does not have the data, it will check the Redis server and if it is there,
                // it will set that value
                .reconnectionStrategy(LocalCachedMapOptions.ReconnectionStrategy.NONE);

        studentsMap = redissonClient.getLocalCachedMap(mapOptions);
    }

    @Test
    public void appServer1() {
        Student student1 = new Student("sam", 10, "Atlanta", List.of(1, 2, 3));
        Student student2 = new Student("jake", 30, "Miami", List.of(10, 20, 30));

        this.studentsMap.put(1, student1);
        this.studentsMap.put(2, student2);

        Flux.interval(Duration.ofSeconds(1)).doOnNext(i -> System.out.println(i + " ===> " + studentsMap.get(1))).subscribe();

        sleep(6000);
    }

    @Test
    public void appServer2() {
        Student student1 = new Student("sam-updated", 10, "Atlanta", List.of(1, 2, 3));
        this.studentsMap.put(1, student1);
    }

    // NOTES:
    // 1. to confirm it uses a local copy, stop the Redis server
    // 2. it can be used a config server (to propagate changes)
}
