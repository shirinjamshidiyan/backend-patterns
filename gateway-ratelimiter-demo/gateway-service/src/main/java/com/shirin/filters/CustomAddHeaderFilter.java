package com.shirin.filters;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class CustomAddHeaderFilter implements GatewayFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        exchange.getResponse().beforeCommit(() -> {
                    exchange.getResponse()
                            .getHeaders()
                            .add("X-Route-Type", "programmatic-style");

                    return Mono.empty();
        });
        return chain.filter(exchange);
    }
}
