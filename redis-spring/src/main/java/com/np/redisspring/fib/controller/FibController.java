package com.np.redisspring.fib.controller;

import com.np.redisspring.fib.service.FibService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("fib")
public class FibController {

    @Autowired
    private FibService fibService;

    @GetMapping("{index}/{name}")
    public Mono<Long> getFib(@PathVariable("index") int index, @PathVariable("name") String name) {
        return Mono.fromSupplier(() -> fibService.getFib(index, name));
    }

    @GetMapping("{index}/clear")
    public Mono<Void> getFib(@PathVariable("index") int index) {
        return Mono.fromRunnable(() -> fibService.clearCache(index));
    }
}
