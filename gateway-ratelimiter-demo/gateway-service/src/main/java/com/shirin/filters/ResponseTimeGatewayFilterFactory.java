package com.shirin.filters;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class ResponseTimeGatewayFilterFactory
        extends AbstractGatewayFilterFactory<Object> {

    public ResponseTimeGatewayFilterFactory() {
        super(Object.class);
    }


    @Override
    public GatewayFilter apply(Object config) {
        return (exchange, chain) -> {

            long start = System.currentTimeMillis();

            exchange.getResponse().beforeCommit(() -> {

                        long duration = System.currentTimeMillis() - start;

                        exchange.getResponse()
                                .getHeaders()
                                .add("X-Response-Time-Ms", String.valueOf(duration));

                        return Mono.empty();


                    });

          return  chain.filter(exchange);
        };
    }
}
