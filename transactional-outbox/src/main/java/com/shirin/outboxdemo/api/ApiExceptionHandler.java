package com.shirin.outboxdemo.api;

import com.shirin.outboxdemo.application.EventSerializationException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.Instant;
import java.util.List;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)  // 400
    public ResponseEntity<ApiErrorResponse> handleValidationError(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {

        List<String> errors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + " : " + error.getDefaultMessage())
                .toList();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body( new ApiErrorResponse(
                        Instant.now(),
                        request.getRequestURI(),
                        "validation Failed" ,
                        errors
                ));
    }


    @ExceptionHandler(HttpMessageNotReadableException.class)  // 400
    public ResponseEntity<ApiErrorResponse> handleUnreadableRequest(
            HttpMessageNotReadableException exception,
            HttpServletRequest request) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(
                        new ApiErrorResponse(
                                Instant.now(),
                                request.getRequestURI(),
                                "Malformed or unreadable JSON request",
                                List.of()
                        )
                );
    }


    @ExceptionHandler(DataIntegrityViolationException.class)  // 409
    public ResponseEntity<ApiErrorResponse> handleDataIntegrity(
            DataIntegrityViolationException exception,
            HttpServletRequest request) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ApiErrorResponse(
                        Instant.now(),
                        request.getRequestURI(),
                        "Database constraint violation",
                        List.of()
                ));
    }


    @ExceptionHandler(IllegalArgumentException.class) //400
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(
            IllegalArgumentException exception,
            HttpServletRequest request) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiErrorResponse(
                        Instant.now(),
                        request.getRequestURI(),
                        exception.getMessage(),
                        List.of()
                ));
    }


    @ExceptionHandler(EventSerializationException.class) // 500
    public ResponseEntity<ApiErrorResponse> handleEventSerialization(
            EventSerializationException exception,
            HttpServletRequest request) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body( new ApiErrorResponse(
                        Instant.now(),
                        request.getRequestURI(),
                        "Failed to serialize outbox event",
                        List.of()
                ));
    }

    @ExceptionHandler(Exception.class) // 500
    public ResponseEntity<ApiErrorResponse> handleGeneralException(
            Exception exception,
            HttpServletRequest request) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body( new ApiErrorResponse(
                        Instant.now(),
                        request.getRequestURI(),
                        "Internal Server exception",
                        List.of()
                ));
    }

}
