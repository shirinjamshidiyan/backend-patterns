package com.shirin.orderservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.time.Duration;

@ConfigurationProperties(prefix = "inventory")
public record InventoryProperties(
        String baseUrl,
        Duration connectTimeout,
        Duration readTimeout
){}
