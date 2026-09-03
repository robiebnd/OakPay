package com.oakpay.trading.market;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

@Service
public class TradingPairService {
    private final TradingPairRepository repository;

    public TradingPairService(TradingPairRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<TradingPairDtos.PairResponse> active() {
        return repository.findAllByStatusOrderBySymbolAsc(TradingPairStatus.ACTIVE).stream().map(this::response).toList();
    }

    @Transactional(readOnly = true)
    public TradingPairDtos.PairResponse get(String symbol) {
        return response(find(symbol));
    }

    @Transactional
    public TradingPairDtos.PairResponse create(TradingPairDtos.CreateRequest request) {
        String symbol = normalize(request.symbol());
        String base = normalize(request.baseCurrency());
        String quote = normalize(request.quoteCurrency());
        validate(symbol, base, quote, request.minPrice(), request.maxPrice(), request.priceTickSize(), request.minQuantity(), request.quantityStepSize());
        if (repository.findBySymbolIgnoreCase(symbol).isPresent() ||
                repository.findByBaseCurrencyIgnoreCaseAndQuoteCurrencyIgnoreCase(base, quote).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Trading pair already exists");
        }
        TradingPair pair = new TradingPair();
        pair.setSymbol(symbol);
        pair.setBaseCurrency(base);
        pair.setQuoteCurrency(quote);
        pair.setMinPrice(request.minPrice());
        pair.setMaxPrice(request.maxPrice());
        pair.setPriceTickSize(request.priceTickSize());
        pair.setMinQuantity(request.minQuantity());
        pair.setQuantityStepSize(request.quantityStepSize());
        pair.setStatus(TradingPairStatus.ACTIVE);
        return response(repository.save(pair));
    }

    @Transactional
    public TradingPairDtos.PairResponse update(String symbol, TradingPairDtos.UpdateRequest request) {
        TradingPair pair = find(symbol);
        validateLimits(request.minPrice(), request.maxPrice(), request.priceTickSize(), request.minQuantity(), request.quantityStepSize());
        pair.setMinPrice(request.minPrice());
        pair.setMaxPrice(request.maxPrice());
        pair.setPriceTickSize(request.priceTickSize());
        pair.setMinQuantity(request.minQuantity());
        pair.setQuantityStepSize(request.quantityStepSize());
        if (request.status() != null) pair.setStatus(request.status());
        return response(repository.save(pair));
    }

    private TradingPair find(String symbol) {
        return repository.findBySymbolIgnoreCase(normalize(symbol))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Trading pair not found"));
    }

    private void validate(String symbol, String base, String quote, BigDecimal minPrice, BigDecimal maxPrice,
                          BigDecimal tick, BigDecimal minQty, BigDecimal step) {
        if (base.equals(quote)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Base and quote currencies must differ");
        if (!symbol.equals(base + quote)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Symbol must equal BASEQUOTE");
        validateLimits(minPrice, maxPrice, tick, minQty, step);
        if (minPrice.compareTo(maxPrice) > 0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Minimum price cannot exceed maximum price");
    }

    private void validateLimits(BigDecimal minPrice, BigDecimal maxPrice, BigDecimal tick, BigDecimal minQty, BigDecimal step) {
        if (minPrice == null || maxPrice == null || tick == null || minQty == null || step == null ||
                minPrice.signum() <= 0 || maxPrice.signum() <= 0 || tick.signum() <= 0 || minQty.signum() <= 0 || step.signum() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Trading pair limits must be positive");
        }
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Value is required");
        return value.trim().toUpperCase(Locale.ROOT);
    }

    private TradingPairDtos.PairResponse response(TradingPair p) {
        return new TradingPairDtos.PairResponse(p.getId(), p.getSymbol(), p.getBaseCurrency(), p.getQuoteCurrency(),
                p.getMinPrice(), p.getMaxPrice(), p.getPriceTickSize(), p.getMinQuantity(), p.getQuantityStepSize(),
                p.getStatus(), p.getCreatedAt(), p.getUpdatedAt());
    }
}
