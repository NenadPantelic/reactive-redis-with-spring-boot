package com.np.redisspring.weather.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
public class ExternalServiceClient {

    @CachePut(value = "weather", key = "index")
    public int getWeatherInfo(int zip) {
        log.info("Fetching the weather info for zip: {}", zip);
        return ThreadLocalRandom.current().nextInt(60, 100);
    }
}
