package com.np.redisperformance.service;

import com.np.redisperformance.entity.Product;
import com.np.redisperformance.service.util.CacheTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class ProductServiceV2 {

    private final CacheTemplate<Integer, Product> cacheTemplate;

    public ProductServiceV2(CacheTemplate<Integer, Product> cacheTemplate) {
        this.cacheTemplate = cacheTemplate;
    }

    // GET
    public Mono<Product> getProduct(int id) {
        return cacheTemplate.get(id);
    }

    // PATCH
    public Mono<Product> updateProduct(int id, Mono<Product> productMono) {
        return productMono.flatMap(p -> cacheTemplate.update(id, p));
    }

    // DELETE
    public Mono<Void> deleteProduct(int id) {
        return cacheTemplate.delete(id);
    }
}
