package com.shirin.inventoryservice.controller;

import com.shirin.api.Inventory.InventoryApi;
import com.shirin.api.Inventory.InventoryResponse;
import com.shirin.api.Inventory.ReserveInventoryRequest;
import com.shirin.api.Inventory.ReserveInventoryResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@Slf4j
public class inventoryController implements InventoryApi {

    private final String instanceId;

    public inventoryController(@Value("${eureka.instance.instance-id}") String instanceId) {
        this.instanceId = instanceId;
    }

    @Override
    public ReserveInventoryResponse reserve(ReserveInventoryRequest request) {

        log.info(" Inventory Reservation for product {}", request.productId());
        return new ReserveInventoryResponse(
                request.requestId(),
                "res-" + UUID.randomUUID(),
                "RESERVED",
                instanceId
        );
    }

    @Override
    public InventoryResponse getInventory(Long productId) {

        log.info("Checking inventory for product {}", productId);
        return new InventoryResponse(productId, 100, true, instanceId);
    }


}
