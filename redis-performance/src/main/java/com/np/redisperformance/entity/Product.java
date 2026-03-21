package com.np.redisperformance.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;
import org.springframework.data.annotation.Id;

@Data
@AllArgsConstructor
@ToString
public class Product {

    @Id
    private Integer id;
    private String description;
    private double price;
}
