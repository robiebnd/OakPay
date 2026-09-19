package com.oakpay.gateway;

import java.util.UUID;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class RequestIdFilter implements org.springframework.cloud.gateway.filter.GlobalFilter, Ordered {
    public static final String HEADER = "X-Request-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String value = exchange.getRequest().getHeaders().getFirst(HEADER);
        if (value == null || value.isBlank() || value.length() > 100) value = UUID.randomUUID().toString();
        ServerHttpRequest request = exchange.getRequest().mutate().header(HEADER, value).build();
        exchange.getResponse().getHeaders().set(HEADER, value);
        return chain.filter(exchange.mutate().request(request).build());
    }

    @Override public int getOrder() { return -100; }
}
