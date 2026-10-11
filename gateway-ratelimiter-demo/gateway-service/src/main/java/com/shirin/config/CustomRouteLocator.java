package com.shirin.config;

import com.shirin.filters.CustomAddHeaderFilter;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

@Component
public class CustomRouteLocator {

    GatewayFilter customFilter1 = (exchange, chain) -> {

        System.out.println(
                "Custom filter: " +
                        exchange.getRequest().getPath()
        );

        return chain.filter(exchange);
    };

    @Bean
    public RouteLocator routeLocator(
            RouteLocatorBuilder builder,
            CustomAddHeaderFilter customFilter2){


       return builder.routes()
                .route("custom-route", r -> r
                                .path("/check/**")
                                .and()
                                .method(HttpMethod.GET)
                                .filters( f-> f
                                        .rewritePath("/check/(?<segment>.*)","/api/v1/inventory/${segment}")
                                        .filter(customFilter1)
                                        .filter(customFilter2)

                                )
                                .uri("lb://INVENTORY-SERVICE")
                )
               .build();

    }


}
