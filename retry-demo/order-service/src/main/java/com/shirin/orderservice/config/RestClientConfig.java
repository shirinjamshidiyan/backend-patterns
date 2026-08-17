package com.shirin.orderservice.config;

import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.util.Timeout;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient inventoryRestClient(RestClient.Builder builder,
                          InventoryProperties properties)
    {

        RequestConfig requestConfig = RequestConfig.custom()
                // Waiting for a free connection from the connection pool
                .setConnectionRequestTimeout( Timeout.ofMilliseconds(properties.connectTimeout().toMillis()))
                // Establishing TCP connection with Inventory Service
                .setConnectTimeout( Timeout.ofMilliseconds(properties.connectTimeout().toMillis()))
                // Waiting for the response from Inventory Service
                .setResponseTimeout(Timeout.ofMilliseconds(properties.readTimeout().toMillis()))
                .build();


        PoolingHttpClientConnectionManager connectionManager =
                PoolingHttpClientConnectionManagerBuilder
                        .create()
                        .setMaxConnTotal(100)
                        .setMaxConnPerRoute(20)
                        .build();


        CloseableHttpClient httpClient = HttpClients.custom()
                .disableAutomaticRetries()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                .build();

        HttpComponentsClientHttpRequestFactory requestFactory =
                new HttpComponentsClientHttpRequestFactory(httpClient);

        return builder
                .requestFactory(requestFactory)
                .baseUrl(properties.baseUrl())
                .build();

    }

}
