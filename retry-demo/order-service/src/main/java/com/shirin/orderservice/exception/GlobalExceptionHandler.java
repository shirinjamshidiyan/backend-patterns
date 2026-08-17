package com.shirin.orderservice.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.http.HttpStatus;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.time.Instant;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceAccessException(
            ResourceAccessException ex,
            HttpServletRequest request
    ){

        Throwable cause = ex;
        while (cause != null) {

            if (cause instanceof SocketTimeoutException) {
                return error(HttpStatus.GATEWAY_TIMEOUT,
                        "Inventory Service did not respond within the configured timeout.",
                        request);
            }
            if (cause instanceof ConnectException) {
                return error(HttpStatus.SERVICE_UNAVAILABLE,
                        "Unable to connect to Inventory Service.",
                        request);
            }
            cause = cause.getCause();
        }
        return error(HttpStatus.BAD_GATEWAY,
                "Inventory Service communication failed.",
                request);

    }

    @ExceptionHandler(HttpServerErrorException.class)
    public ResponseEntity<ApiErrorResponse> handleInventoryServerError(
            HttpServerErrorException ex,
            HttpServletRequest request) {

        HttpStatus status = ex.getStatusCode().is5xxServerError()
                ? HttpStatus.valueOf(ex.getStatusCode().value())
                : HttpStatus.BAD_GATEWAY;

        return error(
                status,
                "Inventory Service returned HTTP " + ex.getStatusCode().value() + ".",
                request
        );
    }

    @ExceptionHandler(InventoryReservationRejectedException.class)
    public ResponseEntity<ApiErrorResponse> handleReservationRejected(
            InventoryReservationRejectedException ex,
            HttpServletRequest request) {

        return error(
                HttpStatus.CONFLICT,
                ex.getMessage(),
                request
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST,
                "Request validation failed.",
                request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST,
                "Request validation failed.",
                request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(
            Exception ex,
            HttpServletRequest request) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR,
                "Unexpected server error.",
                request);
    }


    private ResponseEntity<ApiErrorResponse> error(
            HttpStatus status,
            String message,
            HttpServletRequest request) {
        return ResponseEntity.status(status).body(new ApiErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI()
        ));
    }


}


