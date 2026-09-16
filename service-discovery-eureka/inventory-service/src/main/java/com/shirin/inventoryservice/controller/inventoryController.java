package com.shirin.inventoryservice.controller;

import com.shirin.api.InventoryResponse;
import com.shirin.api.ReserveInventoryRequest;
import com.shirin.api.ReserveInventoryResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("api/v1/inventory")
public class inventoryController {


    private final String instanceId;

    public inventoryController(
            @Value("${eureka.instance.instance-id}") String instanceId) {
        this.instanceId = instanceId;
    }

    @PostMapping("/reservations")
    @ResponseStatus(HttpStatus.CREATED)
    public ReserveInventoryResponse reserve(
            @Valid @RequestBody ReserveInventoryRequest request )
    {
        return new ReserveInventoryResponse(
                request.requestId(),
                "res-" + UUID.randomUUID(),
                "RESERVED",
                instanceId
        );
    }

    @GetMapping("/{productId}")
    public InventoryResponse getInventory(
            @PathVariable(name = "productId")
            @Positive(message = "productId must be greater than 0") Long productId)
    {
        return new InventoryResponse(productId, 100, true, instanceId);
    }




}
