package com.shirin.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
public class RateLimiterConfig {

    @Bean
    public KeyResolver clientIdKeyResolver() {

        return exchange -> {

            String clientId = exchange.getRequest()
                    .getHeaders()
                    .getFirst("X-Client-Id");

            return Mono.justOrEmpty(clientId);
        };
    }
}