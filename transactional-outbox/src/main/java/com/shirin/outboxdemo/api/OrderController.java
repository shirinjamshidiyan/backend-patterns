package com.shirin.outboxdemo.api;

import com.shirin.outboxdemo.application.CreateOrderCommand;
import com.shirin.outboxdemo.application.CreateOrderResult;
import com.shirin.outboxdemo.application.OrderApplicationService;
import com.shirin.outboxdemo.observability.LoggingContext;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
@AllArgsConstructor
@Slf4j
public class OrderController {

    private final OrderApplicationService orderService;

    @PostMapping
    public ResponseEntity<CreateOrderResponse> createOrder(
           @Valid @RequestBody CreateOrderRequest request)
    {

        LoggingContext.putRequestId(request.requestId());

        log.info("Received create order request");

        CreateOrderCommand command = new CreateOrderCommand(
                request.requestId(),
                LoggingContext.currentCorrelationId(),
                request.customerId(),
                request.totalAmount()
        );
        CreateOrderResult result = orderService.createOrder(command);

        LoggingContext.putOrderId(result.orderId());

        log.info("Create order request handled");

        return ResponseEntity
                .status(result.duplicate()? HttpStatus.OK : HttpStatus.CREATED)
                .body(new CreateOrderResponse(
                        result.orderId(),
                        result.duplicate()
                ));

    }
}
