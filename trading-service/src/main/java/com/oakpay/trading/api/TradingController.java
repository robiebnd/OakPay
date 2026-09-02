package com.oakpay.trading.api;

import com.oakpay.trading.order.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/trading")
public class TradingController {
    private final OrderService orderService;

    public TradingController(OrderService orderService) { this.orderService = orderService; }

    @PostMapping("/orders")
    @ResponseStatus(HttpStatus.CREATED)
    public TradingDtos.OrderResponse place(@Valid @RequestBody TradingDtos.CreateOrderRequest request,
                                           Authentication authentication) {
        return orderService.place(userId(authentication), request);
    }

    @DeleteMapping("/orders/{orderId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(@PathVariable UUID orderId, Authentication authentication) {
        orderService.cancel(userId(authentication), orderId);
    }

    @GetMapping("/orders/me")
    public List<TradingDtos.OrderResponse> myOrders(Authentication authentication) {
        return orderService.myOrders(userId(authentication));
    }

    @GetMapping("/order-book")
    public List<TradingDtos.OrderResponse> orderBook(@RequestParam String baseCurrency,
                                                      @RequestParam String quoteCurrency) {
        return orderService.orderBook(baseCurrency, quoteCurrency);
    }

    @GetMapping("/trades/me")
    public List<TradingDtos.TradeResponse> myTrades(Authentication authentication) {
        return orderService.myTrades(userId(authentication));
    }

    private UUID userId(Authentication authentication) { return UUID.fromString(authentication.getName()); }
}
