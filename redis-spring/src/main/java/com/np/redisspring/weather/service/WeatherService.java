package com.np.redisspring.weather.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.stream.IntStream;

@Slf4j
@Service
public class WeatherService {

    @Autowired
    private ExternalServiceClient client;

    @Cacheable("weather")
    public int getInfo(int zip) {
        log.info("Get weather info...");
        return 0;
    }

    // values will be refreshed periodically
    @Scheduled(fixedRate = 10_000) // 10s
    public void update() {
        log.info("Updating...");
        IntStream.rangeClosed(1, 5)
                .forEach(this.client::getWeatherInfo);
    }
}
