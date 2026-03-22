package com.np.redisperformance.service.util;

import com.np.redisperformance.entity.Product;
import com.np.redisperformance.repository.ProductRepository;
import org.redisson.api.LocalCachedMapOptions;
import org.redisson.api.RLocalCachedMap;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class ProductLocalCacheTemplate extends CacheTemplate<Integer, Product> {


    private final ProductRepository productRepository;
    private final RLocalCachedMap<Integer, Product> map;

    public ProductLocalCacheTemplate(ProductRepository productRepository, RedissonClient client) {
        this.productRepository = productRepository;

        LocalCachedMapOptions<Integer, Product> options = LocalCachedMapOptions.<Integer, Product>defaults()
                .syncStrategy(LocalCachedMapOptions.SyncStrategy.UPDATE)
                // clear the cache if the connection is broken
                .reconnectionStrategy(LocalCachedMapOptions.ReconnectionStrategy.CLEAR);
        this.map = client.getLocalCachedMap("product", options);
    }

    @Override
    protected Mono<Product> getFromSource(Integer id) {
        return productRepository.findById(id);
    }

    @Override
    protected Mono<Product> getFromCache(Integer id) {
        return Mono.just(
                // local map, it's not asynchronous call
                map.get(id)
        );
    }

    @Override
    protected Mono<Product> updateSource(Integer id, Product product) {
        return this.productRepository.findById(id)
                .doOnNext(p -> product.setId(id))
                .flatMap(p -> productRepository.save(product));
    }

    @Override
    protected Mono<Product> updateCache(Integer id, Product product) {
        return Mono.create(sink ->
                map.fastPutAsync(id, product)
                        .thenAccept(b -> sink.success(product))
                        .exceptionally(ex -> {
                            sink.error(ex);
                            return null;
                        })
        );
    }

    @Override
    protected Mono<Void> deleteFromSource(Integer id) {
        return productRepository.deleteById(id);
    }

    @Override
    protected Mono<Void> deleteFromCache(Integer id) {
        return Mono.create(sink ->
                map.fastRemoveAsync(id)
                        .thenAccept(b -> sink.success())
                        .exceptionally(ex -> {
                            sink.error(ex);
                            return null;
                        })
        );
    }
}
