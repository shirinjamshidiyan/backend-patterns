package com.shirin.rideservice.api;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.Map;

@RestControllerAdvice
public class GrpcExceptionHandler {


    @ExceptionHandler(StatusRuntimeException.class)
    public ResponseEntity<Map<String, String>> handleGrpcException(
            StatusRuntimeException exception) {

        Status grpcStatus = exception.getStatus();
        HttpStatus httpStatus= switch (grpcStatus.getCode())
        {
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case INVALID_ARGUMENT -> HttpStatus.BAD_REQUEST;
            case DEADLINE_EXCEEDED -> HttpStatus.GATEWAY_TIMEOUT;
            case UNAVAILABLE -> HttpStatus.SERVICE_UNAVAILABLE;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };

        String message = grpcStatus.getDescription() != null
                        ? grpcStatus.getDescription()
                        : "gRPC request failed";

        return ResponseEntity
                .status(httpStatus)
                .body(Map.of(
                        "grpcStatus", grpcStatus.getCode().name(),
                        "message", message));

    }
}
