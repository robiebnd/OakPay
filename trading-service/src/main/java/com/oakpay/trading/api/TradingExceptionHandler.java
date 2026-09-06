package com.oakpay.trading.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class TradingExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> badRequest(IllegalArgumentException ex) {
        return Map.of("timestamp", Instant.now(), "status", 400, "error", "Bad Request", "message", ex.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, Object> conflict(IllegalStateException ex) {
        return Map.of("timestamp", Instant.now(), "status", 409, "error", "Conflict", "message", ex.getMessage());
    }

    @ExceptionHandler(HttpClientErrorException.Conflict.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, Object> walletConflict(HttpClientErrorException.Conflict ex) {
        String message = extractMessage(ex.getResponseBodyAsString());
        return Map.of("timestamp", Instant.now(), "status", 409, "error", "Conflict", "message", message);
    }

    private String extractMessage(String body) {
        if (body == null || body.isBlank()) {
            return "Downstream service rejected the request";
        }
        int marker = body.indexOf("\"message\"");
        if (marker >= 0) {
            int colon = body.indexOf(':', marker);
            int firstQuote = body.indexOf('\"', colon + 1);
            int secondQuote = firstQuote >= 0 ? body.indexOf('\"', firstQuote + 1) : -1;
            if (firstQuote >= 0 && secondQuote > firstQuote) {
                return body.substring(firstQuote + 1, secondQuote);
            }
        }
        return body;
    }
}
