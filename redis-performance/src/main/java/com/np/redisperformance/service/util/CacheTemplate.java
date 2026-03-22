package com.np.redisperformance.service.util;

import com.np.redisperformance.entity.Product;
import reactor.core.publisher.Mono;

// Key, Entity (Value)
public abstract class CacheTemplate<K, V> {

    public Mono<V> get(K key) {
        return getFromCache(key)
                .switchIfEmpty(
                        getFromSource(key).flatMap(e -> updateCache(key, e))
                );
    }

    public Mono<V> update(K key, V value) {
        // first update the source
        return updateSource(key, value)
//                .flatMap(e -> updateCache(key, value));
                .flatMap(e -> deleteFromCache(key).thenReturn(e));
    }

    public Mono<Void> delete(K key) {
        return deleteFromSource(key)
                .then(deleteFromCache(key));
    }


    protected abstract Mono<Product> updateSource(Integer id, Product product);

    abstract protected Mono<V> getFromSource(K key);

    abstract protected Mono<V> getFromCache(K key);

    abstract protected Mono<V> updateSource(K key, V value);

    abstract protected Mono<V> updateCache(K key, V value);

    abstract protected Mono<Void> deleteFromSource(K key);

    abstract protected Mono<Void> deleteFromCache(K key);
}
