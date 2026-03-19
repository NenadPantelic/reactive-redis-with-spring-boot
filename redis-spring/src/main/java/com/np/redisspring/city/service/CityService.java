package com.np.redisspring.city.service;

import com.np.redisspring.city.client.CityClient;
import com.np.redisspring.city.dto.City;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMapCache;
import org.redisson.api.RMapCacheReactive;
import org.redisson.api.RMapReactive;
import org.redisson.api.RedissonReactiveClient;
import org.redisson.codec.TypedJsonJacksonCodec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class CityService {

    @Autowired
    private CityClient cityClient;


    // so we can evict it
    // RMapReactive does not support it
    private RMapCacheReactive<String, City> cityMap;

    // Redis does not provide a way to expire a certain key in hash; just the whole key
    // Redisson on the other hand does (it creates a dedicated hash for that)
    public CityService(RedissonReactiveClient client) {
        this.cityMap = client.getMapCache("city", new TypedJsonJacksonCodec(String.class, City.class));
    }

    public Mono<City> getCity(final String zipCode) {
        return this.cityMap.get(zipCode)
                // if empty, retrieve it from the client
                .switchIfEmpty(this.cityClient.getCity(zipCode)
                        // fastPut returns a Mono<Boolean>,
                        // put returns a Mono<T>
                        .flatMap(c -> this.cityMap.fastPut(zipCode, c, 10, TimeUnit.SECONDS).thenReturn(c)));
    }
}
