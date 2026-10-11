package com.shirin.api.Inventory;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

public interface InventoryApi {

    @PostMapping("api/v1/inventory/reservations")
    @ResponseStatus(HttpStatus.CREATED)
    ReserveInventoryResponse reserve( @Valid @RequestBody ReserveInventoryRequest request );

    @GetMapping("api/v1/inventory/{productId}")
    InventoryResponse getInventory(@PathVariable(name = "productId") @Positive Long productId);

}
