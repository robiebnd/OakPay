package com.oakpay.trading.market;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/markets")
public class TradingPairController {
    private final TradingPairService service;

    public TradingPairController(TradingPairService service) {
        this.service = service;
    }

    @GetMapping
    public List<TradingPairDtos.PairResponse> active() {
        return service.active();
    }

    @GetMapping("/{symbol}")
    public TradingPairDtos.PairResponse get(@PathVariable String symbol) {
        return service.get(symbol);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TradingPairDtos.PairResponse create(@Valid @RequestBody TradingPairDtos.CreateRequest request) {
        return service.create(request);
    }

    @PutMapping("/{symbol}")
    public TradingPairDtos.PairResponse update(@PathVariable String symbol,
                                               @Valid @RequestBody TradingPairDtos.UpdateRequest request) {
        return service.update(symbol, request);
    }
}
