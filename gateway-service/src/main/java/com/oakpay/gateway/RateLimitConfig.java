package com.oakpay.gateway;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
public class RateLimitConfig {

    @Bean
    KeyResolver ipKeyResolver() {
        return exchange -> {
            var address = exchange.getRequest().getRemoteAddress();
            String host = address != null && address.getAddress() != null
                    ? address.getAddress().getHostAddress()
                    : "unknown";
            return Mono.just(host);
        };
    }
}
