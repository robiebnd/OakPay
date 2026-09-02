package com.oakpay.trading.order;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
    List<Order> findAllByUserIdOrderByCreatedAtDesc(UUID userId);
    List<Order> findAllByBaseCurrencyAndQuoteCurrencyAndSideAndStatusOrderByCreatedAtAsc(
            String baseCurrency, String quoteCurrency, OrderSide side, OrderStatus status);
}
