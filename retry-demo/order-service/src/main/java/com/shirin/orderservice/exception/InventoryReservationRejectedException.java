package com.shirin.orderservice.exception;

public class InventoryReservationRejectedException extends RuntimeException {

    public InventoryReservationRejectedException(String message) {
        super(message);
    }
}
