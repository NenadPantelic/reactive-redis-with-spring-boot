package com.np.redisson.test.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.np.redisson.test.dto.Restaurant;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

public class RestaurantUtil {

    public static List<Restaurant> getRestaurants() {
        ObjectMapper objectMapper = new ObjectMapper();

        InputStream stream = RestaurantUtil.class.getClassLoader().getResourceAsStream("restaurants.json");
        try {
            return objectMapper.readValue(stream, new TypeReference<List<Restaurant>>() {
            });
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

}
