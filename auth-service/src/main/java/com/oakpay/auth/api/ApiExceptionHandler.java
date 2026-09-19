package com.oakpay.auth.api;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.HttpStatus;
import java.time.Instant;

@RestControllerAdvice
public class ApiExceptionHandler {
    private ResponseEntity<Map<String, Object>> response(HttpStatus status, String code, String message) {
        return ResponseEntity.status(status).body(Map.of(
                "timestamp", Instant.now(),
                "status", status.value(),
                "error", code,
                "message", message
        ));
    }

    @ExceptionHandler(BadCredentialsException.class)
    ResponseEntity<Map<String, Object>> badCredentials() {
        return response(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Invalid email or password");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<Map<String, Object>> badRequest(IllegalArgumentException ex) {
        HttpStatus status = ex.getMessage() != null && ex.getMessage().contains("already exists")
                ? HttpStatus.CONFLICT : HttpStatus.BAD_REQUEST;
        return response(status, status == HttpStatus.CONFLICT ? "CONFLICT" : "BAD_REQUEST", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Map<String, Object>> validation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElse("Invalid request");
        return response(HttpStatus.BAD_REQUEST, "BAD_REQUEST", message);
    }
    @ExceptionHandler(IllegalStateException.class)
    ResponseEntity<Map<String, Object>> conflict(IllegalStateException ex) {
        return response(HttpStatus.CONFLICT, "CONFLICT", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<Map<String, Object>> unexpected(Exception ex) {
        return response(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "An unexpected error occurred");
    }
}
