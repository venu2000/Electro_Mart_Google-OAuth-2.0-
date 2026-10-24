package com.electromart.dto;

import com.electromart.entity.Product;

import java.math.BigDecimal;
import java.time.Instant;

/** Public product shape. Entities are never returned directly from controllers. */
public record ProductResponse(
        Long id,
        String name,
        String brand,
        String category,
        String description,
        BigDecimal price,
        String imageUrl,
        Integer stock,
        Instant createdAt
) {

    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getBrand(),
                product.getCategory(),
                product.getDescription(),
                product.getPrice(),
                product.getImageUrl(),
                product.getStock(),
                product.getCreatedAt());
    }
}
