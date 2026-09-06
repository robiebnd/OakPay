package com.oakpay.trading.api;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class TradingExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String,Object> badRequest(IllegalArgumentException ex,HttpServletRequest request){return error(400,"BAD_REQUEST",ex.getMessage(),request.getRequestURI());}

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String,Object> conflict(IllegalStateException ex,HttpServletRequest request){return error(409,"CONFLICT",ex.getMessage(),request.getRequestURI());}

    @ExceptionHandler(HttpClientErrorException.Conflict.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String,Object> walletConflict(HttpClientErrorException.Conflict ex,HttpServletRequest request){return error(409,"CONFLICT",extractMessage(ex.getResponseBodyAsString()),request.getRequestURI());}

    private Map<String,Object> error(int status,String code,String message,String path){Map<String,Object> body=new LinkedHashMap<>();body.put("timestamp",Instant.now());body.put("status",status);body.put("error",code);body.put("message",message==null?"Request failed":message);body.put("path",path);return body;}
    private String extractMessage(String body){if(body==null||body.isBlank())return "Downstream service rejected the request";int marker=body.indexOf("\"message\"");if(marker>=0){int colon=body.indexOf(':',marker);int first=body.indexOf('"',colon+1);int second=first>=0?body.indexOf('"',first+1):-1;if(first>=0&&second>first)return body.substring(first+1,second);}return body;}
}
