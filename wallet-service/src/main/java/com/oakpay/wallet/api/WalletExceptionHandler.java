package com.oakpay.wallet.api;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class WalletExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String,Object> badRequest(IllegalArgumentException ex,HttpServletRequest request){return error(400,"BAD_REQUEST",ex.getMessage(),request.getRequestURI());}

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String,Object> conflict(IllegalStateException ex,HttpServletRequest request){return error(409,"CONFLICT",ex.getMessage(),request.getRequestURI());}

    private Map<String,Object> error(int status,String code,String message,String path){Map<String,Object> body=new LinkedHashMap<>();body.put("timestamp",Instant.now());body.put("status",status);body.put("error",code);body.put("message",message==null?"Request failed":message);body.put("path",path);return body;}
}
