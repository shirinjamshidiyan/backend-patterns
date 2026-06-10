package com.shirin.outboxdemo.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shirin.outboxdemo.contracts.EventEnvelope;
import com.shirin.outboxdemo.contracts.EventSources;
import com.shirin.outboxdemo.contracts.EventTypes;
import com.shirin.outboxdemo.domain.Order;
import com.shirin.outboxdemo.domain.OrderRepository;
import com.shirin.outboxdemo.outbox.OutboxEvent;
import com.shirin.outboxdemo.outbox.OutboxEventRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class OrderCreationTxService  {

    private final OrderRepository orderRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public CreateOrderResult createNewOrder(CreateOrderCommand command) {

        UUID orderId = UUID.randomUUID();

        Order order = new Order(
                orderId,
                command.requestId(),
                command.customerId(),
                command.totalAmount()
        );

        //Save Business Data
        orderRepository.saveAndFlush(order);

        UUID eventId = UUID.randomUUID();

        OrderCreatedPayload payload= new OrderCreatedPayload(
                orderId,
                command.customerId(),
                command.totalAmount());


        var envelopeWithPayload = new EventEnvelope<OrderCreatedPayload>(
                eventId,
                EventTypes.ORDER_CREATED,
                1,
                command.correlationId(),
                null, //the first event of the creation-flow doesn't have causationId
                Instant.now(),
                EventSources.ORDER_SERVICE,
                payload);

        //Save Outbox event in the same transaction as business data
        outboxEventRepository.save(
                OutboxEvent.createPendingEvent(
                        eventId,
                        "Order",
                        orderId,  // here, we use it as key in kafka
                        EventTypes.ORDER_CREATED,
                        toJson(envelopeWithPayload)
        ));


        log.info("Order created event stored in outbox");

        return new CreateOrderResult(orderId, false);
    }

    private String toJson(Object envelope) {
        try {
            return objectMapper.writeValueAsString(envelope);
        } catch (JsonProcessingException ex) {
            throw new EventSerializationException("Failed to serialize outgoing order envelope", ex);
        }
    }



}
