package com.shirin.outboxdemo.domain;

import jakarta.persistence.*;
import lombok.Getter;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter
public class Order {

    @Id
    private UUID id;

    @Column(name = "request_id", nullable = false, unique = true)
    private UUID requestId;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;

    @Column(name = "created_at", insertable = false, updatable = false, nullable = false)
    private Instant createdAt;


    protected Order() {}
    public Order(
                UUID id,
                UUID requestId,
                UUID customerId,
                BigDecimal totalAmount
        ) {
            this.id = id;
            this.requestId = requestId;
            this.customerId = customerId;
            this.totalAmount = totalAmount;
            this.status = OrderStatus.CREATED;
        }

}


