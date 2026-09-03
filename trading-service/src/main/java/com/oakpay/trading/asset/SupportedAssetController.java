package com.oakpay.trading.asset;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/assets")
public class SupportedAssetController {
    private final SupportedAssetService service;

    public SupportedAssetController(SupportedAssetService service) {
        this.service = service;
    }

    @GetMapping
    public List<SupportedAssetDtos.AssetResponse> active() {
        return service.active();
    }

    @GetMapping("/{symbol}")
    public SupportedAssetDtos.AssetResponse get(@PathVariable String symbol) {
        return service.get(symbol);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SupportedAssetDtos.AssetResponse create(@Valid @RequestBody SupportedAssetDtos.CreateRequest request) {
        return service.create(request);
    }

    @PutMapping("/{symbol}")
    public SupportedAssetDtos.AssetResponse update(@PathVariable String symbol,
                                                   @Valid @RequestBody SupportedAssetDtos.UpdateRequest request) {
        return service.update(symbol, request);
    }
}
