package com.oakpay.trading.p2p;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Local-development read-only transaction feed for the admin portal.
 * No user/admin authentication is required for this endpoint.
 */
@RestController
@RequestMapping("/api/v1/p2p/public-admin/transactions")
public class PublicP2PTransactionsController {
    private final P2PTradeRepository tradeRepository;

    public PublicP2PTransactionsController(P2PTradeRepository tradeRepository) {
        this.tradeRepository = tradeRepository;
    }

    @GetMapping
    public List<P2PTradeDtos.TradeResponse> transactions(
            @RequestParam(defaultValue = "100") int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 200);
        return tradeRepository
                .findAll(PageRequest.of(0, safeLimit, Sort.by(Sort.Direction.DESC, "createdAt")))
                .map(P2PTradeDtos.TradeResponse::from)
                .toList();
    }
}
