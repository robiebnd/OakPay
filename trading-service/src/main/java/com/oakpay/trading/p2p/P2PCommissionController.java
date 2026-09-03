package com.oakpay.trading.p2p;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/p2p/trades")
public class P2PCommissionController {
    private final P2PTradeRepository tradeRepository;
    private final P2PCommissionService commissionService;

    public P2PCommissionController(P2PTradeRepository tradeRepository, P2PCommissionService commissionService) {
        this.tradeRepository = tradeRepository;
        this.commissionService = commissionService;
    }

    @GetMapping("/{tradeId}/commission")
    public P2PCommissionDtos.Response get(@PathVariable UUID tradeId, Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        P2PTrade trade = tradeRepository.findById(tradeId)
                .orElseThrow(() -> new IllegalArgumentException("P2P trade not found"));
        if (!trade.getBuyerId().equals(userId) && !trade.getSellerId().equals(userId)) {
            throw new IllegalArgumentException("Trade does not belong to user");
        }
        return P2PCommissionDtos.Response.from(commissionService.getByTrade(tradeId));
    }
}
