package com.shirin.orderservice.controller;

import com.shirin.api.order.CreateOrderRequest;
import com.shirin.api.Inventory.InventoryResponse;
import com.shirin.api.order.CreateOrderResponse;
import com.shirin.orderservice.service.OrderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<CreateOrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request)
    {

        log.info("Create request for {}", request.requestId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.createOrder(request));
    }

    @GetMapping("/inventory/{productId}")
    public ResponseEntity<InventoryResponse> checkInventory(
            @PathVariable(name = "productId")
            @Positive Long productId)

    {
        log.info("checkInventory request for {}", productId);
        return ResponseEntity
                .ok(orderService.checkInventory(productId));
    }


}
