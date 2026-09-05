package com.oakpay.trading.p2p;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/p2p/ads")
public class AdvertisementController {
    private final AdvertisementService service;

    public AdvertisementController(AdvertisementService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<AdvertisementDtos.AdResponse> create(@RequestBody AdvertisementDtos.CreateRequest request,
                                                                Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(userId(authentication), request));
    }

    @GetMapping
    public List<AdvertisementDtos.AdResponse> search(@RequestParam OrderSide side,
                                                     @RequestParam String asset,
                                                     @RequestParam String fiatCurrency,
                                                     @RequestParam(defaultValue = "50") int limit) {
        return service.search(side, asset, fiatCurrency, limit);
    }

    @GetMapping("/mine")
    public List<AdvertisementDtos.AdResponse> mine(Authentication authentication) {
        return service.mine(userId(authentication));
    }

    @GetMapping("/{adId}")
    public AdvertisementDtos.AdResponse get(@PathVariable UUID adId) {
        return service.get(adId);
    }

    @PutMapping("/{adId}")
    public AdvertisementDtos.AdResponse update(@PathVariable UUID adId,
                                                @RequestBody AdvertisementDtos.UpdateRequest request,
                                                Authentication authentication) {
        return service.update(userId(authentication), adId, request);
    }

    @PostMapping("/{adId}/pause")
    public AdvertisementDtos.AdResponse pause(@PathVariable UUID adId, Authentication authentication) {
        return service.pause(userId(authentication), adId);
    }

    @PostMapping("/{adId}/close")
    public AdvertisementDtos.AdResponse close(@PathVariable UUID adId, Authentication authentication) {
        return service.close(userId(authentication), adId);
    }

    @PostMapping("/{adId}/take")
    public ResponseEntity<P2PTradeDtos.TradeResponse> take(@PathVariable UUID adId,
                                                            @RequestBody AdvertisementDtos.TakeRequest request,
                                                            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.take(userId(authentication), adId, request));
    }

    private UUID userId(Authentication authentication) { return UUID.fromString(authentication.getName()); }
}
