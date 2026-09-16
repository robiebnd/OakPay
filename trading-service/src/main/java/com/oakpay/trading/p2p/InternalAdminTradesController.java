package com.oakpay.trading.p2p;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/internal/admin/trades")
public class InternalAdminTradesController {
    private final P2PTradeRepository tradeRepository;
    private final String internalSecret;

    public InternalAdminTradesController(
            P2PTradeRepository tradeRepository,
            @Value("${oakpay.internal-secret}") String internalSecret) {
        this.tradeRepository = tradeRepository;
        this.internalSecret = internalSecret;
    }

    @GetMapping
    public List<P2PTradeDtos.TradeResponse> trades(
            @RequestHeader(value = "X-OakPay-Internal-Secret", required = false) String suppliedSecret,
            @RequestParam(defaultValue = "100") int limit) {
        requireInternalSecret(suppliedSecret);
        int safeLimit = Math.min(Math.max(limit, 1), 200);
        return tradeRepository.findAll(PageRequest.of(0, safeLimit, Sort.by(Sort.Direction.DESC, "createdAt")))
                .map(P2PTradeDtos.TradeResponse::from)
                .toList();
    }

    private void requireInternalSecret(String suppliedSecret) {
        if (suppliedSecret == null || internalSecret == null || !suppliedSecret.equals(internalSecret)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Internal access required");
        }
    }
}
