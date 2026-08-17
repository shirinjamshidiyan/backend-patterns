package com.shirin.orderservice.controller;

import com.shirin.orderservice.dto.CreateOrderRequest;
import com.shirin.orderservice.dto.CreateOrderResponse;
import com.shirin.orderservice.dto.InventoryMode;
import com.shirin.orderservice.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping

    public ResponseEntity<CreateOrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            @RequestParam(defaultValue = "SUCCESS") InventoryMode mode) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.createOrder(request, mode));
    }


}
