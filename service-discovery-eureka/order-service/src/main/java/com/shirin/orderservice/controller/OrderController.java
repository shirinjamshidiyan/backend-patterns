package com.shirin.orderservice.controller;

import com.shirin.api.CreateOrderRequest;
import com.shirin.api.InventoryResponse;
import com.shirin.api.CreateOrderResponse;
import com.shirin.orderservice.service.OrderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<CreateOrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request)
    {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.createOrder(request));
    }

    @GetMapping("/inventory/{productId}")
    public ResponseEntity<InventoryResponse> checkInventory(
            @PathVariable(name = "productId")
            @Positive(message = "productId must be greater than 0") Long productId)

    {
        return ResponseEntity
                .ok(orderService.checkInventory(productId));
    }



}
