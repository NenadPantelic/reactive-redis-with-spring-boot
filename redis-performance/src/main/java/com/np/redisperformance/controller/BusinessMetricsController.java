package com.np.redisperformance.controller;

import com.np.redisperformance.service.BusinessMetricsService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.awt.*;
import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("products/metrics")
public class BusinessMetricsController {

    private final BusinessMetricsService businessMetricsService;

    public BusinessMetricsController(BusinessMetricsService businessMetricsService) {
        this.businessMetricsService = businessMetricsService;
    }

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Map<Integer, Double>> getMetrics() {
        return this.businessMetricsService.topNProducts(3)
                // repeat does it again and again if it doesn't have any condition
                .repeatWhen(l -> Flux.interval(Duration.ofSeconds(3)));
    }
}
