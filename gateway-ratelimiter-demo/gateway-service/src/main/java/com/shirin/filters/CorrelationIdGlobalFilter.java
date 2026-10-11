package com.shirin.filters;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class CorrelationIdGlobalFilter implements GlobalFilter, Ordered {

    private static final String HEADER = "X-Correlation-ID";

    @Override
    public Mono<Void> filter(
            ServerWebExchange exchange,
            GatewayFilterChain chain) {


        String corrId = exchange.getRequest().getHeaders().getFirst(HEADER);

        if(corrId == null || corrId.isBlank()) {
            corrId  = UUID.randomUUID().toString();
        }

        ServerHttpRequest newReq = exchange.getRequest().mutate().header(HEADER, corrId).build();

        return chain.filter(exchange.mutate().request(newReq).build());

    }

    @Override
    public int getOrder() {
        return -10;
    }
}
