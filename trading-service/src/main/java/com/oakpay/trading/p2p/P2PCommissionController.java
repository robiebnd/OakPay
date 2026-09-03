package com.oakpay.trading.p2p;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/p2p/trades")
public class P2PCommissionController {
    private final P2PTradeRepository tradeRepository;
    private final P2PCommissionService commissionService;
    private final String adminSecret;

    public P2PCommissionController(P2PTradeRepository tradeRepository,
                                   P2PCommissionService commissionService,
                                   @Value("${oakpay.admin.dispute-secret}") String adminSecret) {
        this.tradeRepository = tradeRepository;
        this.commissionService = commissionService;
        this.adminSecret = adminSecret;
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

    @PostMapping("/{tradeId}/commission/collect")
    @ResponseStatus(HttpStatus.OK)
    public P2PCommissionDtos.Response collect(@PathVariable UUID tradeId,
                                                @Valid @RequestBody P2PCommissionDtos.CollectionRequest request,
                                                @RequestHeader(value = "X-OakPay-Admin-Secret", required = false) String suppliedSecret) {
        requireAdmin(suppliedSecret);
        if (!tradeRepository.existsById(tradeId)) {
            throw new IllegalArgumentException("P2P trade not found");
        }
        return P2PCommissionDtos.Response.from(commissionService.collect(tradeId, request));
    }

    private void requireAdmin(String supplied) {
        if (adminSecret == null || adminSecret.isBlank() || supplied == null ||
                !MessageDigest.isEqual(adminSecret.getBytes(StandardCharsets.UTF_8), supplied.getBytes(StandardCharsets.UTF_8))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid admin credential");
        }
    }
}
