package com.np.redisson.test.dto;

public record Restaurant(String id,
                         String city,
                         double latitude,
                         double longitude,
                         String name,
                         String zip) {
}
