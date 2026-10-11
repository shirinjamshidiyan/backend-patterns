package com.shirin.orderservice.interceptors;

import feign.RequestInterceptor;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeneralFeignInterceptorConfig {

    private static final String HEADER = "X-Correlation-ID";
    private static final String MDC_KEY = "correlationId";

    @Bean
    RequestInterceptor correlationIdInterceptor()
    {
        return requestTemplate  -> {

            String corrId = MDC.get(MDC_KEY);

            if (corrId != null) {
                requestTemplate.header(HEADER, corrId);
            }
        };

    }
}
