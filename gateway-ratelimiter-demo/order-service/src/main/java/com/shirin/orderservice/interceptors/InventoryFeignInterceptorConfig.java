package com.shirin.orderservice.interceptors;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;

// intentionally NOT @Configuration
public class InventoryFeignInterceptorConfig {

    @Bean
    public RequestInterceptor inventoryClientInterceptor() {

        return template -> template.header("X-Calling-Service", "order-service");
    }
}
