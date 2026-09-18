package com.oakpay.trading.p2p;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/p2p/reporting/transactions")
public class P2PReportingController {

    private final P2PTradeRepository tradeRepository;

    public P2PReportingController(P2PTradeRepository tradeRepository) {
        this.tradeRepository = tradeRepository;
    }

    @GetMapping
    public List<P2PTradeDtos.TradeResponse> transactions(
            @RequestParam(defaultValue = "200") int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 200);
        return tradeRepository
                .findAll(PageRequest.of(0, safeLimit, Sort.by(Sort.Direction.DESC, "createdAt")))
                .map(P2PTradeDtos.TradeResponse::from)
                .toList();
    }
}
