package com.np.redisson.test;

import com.np.redisson.test.dto.GeoLocation;
import com.np.redisson.test.dto.Restaurant;
import com.np.redisson.test.util.RestaurantUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.redisson.api.GeoUnit;
import org.redisson.api.RGeoReactive;
import org.redisson.api.RMapReactive;
import org.redisson.api.geo.GeoSearchArgs;
import org.redisson.api.geo.OptionalGeoSearch;
import org.redisson.codec.TypedJsonJacksonCodec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.function.Function;

public class Lec17GeoSpatialTest extends BaseTest {

    private RGeoReactive<Restaurant> geo;
    // zip -> geolocation
    private RMapReactive<String, GeoLocation> geolocationMap;

    @BeforeAll
    public void setGeo() {
        this.geo = this.client.getGeo(
                "restaurants", new TypedJsonJacksonCodec(Restaurant.class)
        );
        this.geolocationMap = this.client.getMap(
                "us:texas", new TypedJsonJacksonCodec(String.class, GeoLocation.class)
        );
    }

    @Test
    public void testAdd() {
        RGeoReactive<Restaurant> geo = this.client.getGeo(
                "restaurants", new TypedJsonJacksonCodec(Restaurant.class)
        );

        Mono<Void> mono = Flux.fromIterable(RestaurantUtil.getRestaurants())
                .flatMap(r -> geo.add(r.longitude(), r.latitude(), r))
                .then();
        StepVerifier.create(mono).verifyComplete();

        // find me a list of restaurants in this area
        OptionalGeoSearch radius = GeoSearchArgs.from(
                -96.80539,
                32.78136
        ).radius(3, GeoUnit.MILES);

        geo.search(radius)
                .flatMapIterable(Function.identity())
                .doOnNext(System.out::println)
                .subscribe();
    }

    @Test
    public void add() {
        Mono<Void> mono = Flux.fromIterable(RestaurantUtil.getRestaurants())
                .flatMap(r -> geo.add(r.longitude(), r.latitude(), r).thenReturn(r))
                .flatMap(r -> this.geolocationMap.fastPut(r.zip(), new GeoLocation(r.longitude(), r.latitude())))
                .then();
        StepVerifier.create(mono).verifyComplete();
    }

    @Test
    public void search() {
        Mono<Void> mono = this.geolocationMap.get("75224")
                .map(gl -> GeoSearchArgs.from(gl.longitude(), gl.latitude()).radius(5, GeoUnit.MILES))
                .flatMap(gl -> this.geo.search(gl))
                .flatMapIterable(Function.identity())
                .doOnNext(System.out::println)
                .then();

        // wait until it is complete
        StepVerifier.create(mono).verifyComplete();
    }
}
