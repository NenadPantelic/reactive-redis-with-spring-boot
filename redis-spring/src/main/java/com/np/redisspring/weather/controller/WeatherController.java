package com.np.redisspring.weather.controller;

import com.np.redisspring.weather.service.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("weather")
public class WeatherController {

    @Autowired
    private WeatherService weatherService;

    @GetMapping("{zip}")
    public Mono<Integer> getWeather(@PathVariable("zip") int zip) {
        return Mono.fromSupplier(() -> weatherService.getInfo(zip));
    }
}
