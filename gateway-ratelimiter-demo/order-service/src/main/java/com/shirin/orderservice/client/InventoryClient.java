package com.shirin.orderservice.client;

import com.shirin.api.Inventory.InventoryApi;
import com.shirin.orderservice.interceptors.InventoryFeignInterceptorConfig;
import org.springframework.cloud.openfeign.FeignClient;


@FeignClient(
        name = "inventory-service",
        configuration = InventoryFeignInterceptorConfig.class
)
public interface InventoryClient extends InventoryApi {

}
