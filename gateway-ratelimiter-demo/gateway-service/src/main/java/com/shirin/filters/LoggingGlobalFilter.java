package com.shirin.filters;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class LoggingGlobalFilter implements GlobalFilter, Ordered {
    private static final String HEADER = "X-Correlation-ID";
    @Override
    public Mono<Void> filter(ServerWebExchange exchange,
                             GatewayFilterChain chain) {

        String corrId = exchange.getRequest().getHeaders().getFirst(HEADER);
        String uri= exchange.getRequest().getURI().toString();

        log.info("Request CorrelationId : {} , uri: {} " ,corrId , uri);
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -5;
    }
}


