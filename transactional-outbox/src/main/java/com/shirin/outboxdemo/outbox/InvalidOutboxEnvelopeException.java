package com.shirin.outboxdemo.outbox;

public class InvalidOutboxEnvelopeException extends RuntimeException
{
    public InvalidOutboxEnvelopeException(String message, Throwable cause) {
        super(message, cause);
    }
}
