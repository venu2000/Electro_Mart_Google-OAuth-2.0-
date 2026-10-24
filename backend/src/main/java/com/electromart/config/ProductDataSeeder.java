package com.electromart.config;

import com.electromart.entity.Product;
import com.electromart.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Seeds exactly 10 sample products on startup.
 * Idempotent: a product is inserted only if no product with that name exists yet,
 * so restarting the application never creates duplicates.
 */
@Component
public class ProductDataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ProductDataSeeder.class);

    private final ProductRepository productRepository;

    public ProductDataSeeder(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        int inserted = 0;
        for (Product product : sampleProducts()) {
            if (!productRepository.existsByName(product.getName())) {
                productRepository.save(product);
                inserted++;
            }
        }
        log.info("Product seed complete: {} inserted, {} total expected", inserted, sampleProducts().size());
    }

    private List<Product> sampleProducts() {
        // Images are local placeholders served by the React dev server from frontend/public/images/products.
        return List.of(
                new Product("ProBook 14 Laptop", "VoltTech", "Computers",
                        "14-inch ultrabook with 16 GB RAM, 512 GB SSD and all-day battery.",
                        new BigDecimal("899.99"), "/images/products/laptop.svg", 12),
                new Product("Nova X5 Smartphone", "Nexora", "Mobiles",
                        "6.5-inch OLED display, triple camera and fast charging.",
                        new BigDecimal("649.00"), "/images/products/smartphone.svg", 25),
                new Product("Aero Tab 11 Tablet", "Nexora", "Tablets",
                        "11-inch tablet for reading, streaming and note-taking.",
                        new BigDecimal("429.50"), "/images/products/tablet.svg", 18),
                new Product("Pulse Watch S2 Smartwatch", "FitLoop", "Wearables",
                        "Health tracking, GPS and a week-long battery life.",
                        new BigDecimal("199.99"), "/images/products/smartwatch.svg", 30),
                new Product("Quiet Pro Wireless Headphones", "AuraSound", "Audio",
                        "Over-ear headphones with active noise cancellation.",
                        new BigDecimal("249.00"), "/images/products/headphones.svg", 20),
                new Product("Boom Mini Bluetooth Speaker", "AuraSound", "Audio",
                        "Pocket-sized speaker with waterproof body and 12-hour playback.",
                        new BigDecimal("59.99"), "/images/products/speaker.svg", 40),
                new Product("Swift G7 Gaming Mouse", "VoltTech", "Accessories",
                        "Lightweight 16000 DPI optical mouse with programmable buttons.",
                        new BigDecimal("49.99"), "/images/products/mouse.svg", 35),
                new Product("Click K9 Mechanical Keyboard", "VoltTech", "Accessories",
                        "Hot-swappable mechanical keyboard with RGB backlight.",
                        new BigDecimal("109.00"), "/images/products/keyboard.svg", 22),
                new Product("UltraView 27 Monitor", "Lumen", "Displays",
                        "27-inch QHD IPS monitor with 144 Hz refresh rate.",
                        new BigDecimal("279.00"), "/images/products/monitor.svg", 9),
                new Product("HD Stream Webcam 1080p", "Lumen", "Accessories",
                        "Full HD webcam with built-in microphone for video calls.",
                        new BigDecimal("69.99"), "/images/products/webcam.svg", 0)
        );
    }
}
