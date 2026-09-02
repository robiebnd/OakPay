package com.oakpay.wallet.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class WalletExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse badRequest(IllegalArgumentException ex) {
        return new ErrorResponse("BAD_REQUEST", ex.getMessage(), LocalDateTime.now());
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse conflict(IllegalStateException ex) {
        return new ErrorResponse("CONFLICT", ex.getMessage(), LocalDateTime.now());
    }

    public record ErrorResponse(String code, String message, LocalDateTime timestamp) {}
}
