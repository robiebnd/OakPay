package com.oakpay.trading.p2p;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/p2p/reputation")
public class P2PReputationController {

    private final P2PReputationService reputationService;

    public P2PReputationController(P2PReputationService reputationService) {
        this.reputationService = reputationService;
    }

    @PostMapping("/../trades/{tradeId}/rating")
    @ResponseStatus(HttpStatus.CREATED)
    public P2PReputationDtos.RatingResponse rate(Authentication authentication,
                                                  @PathVariable UUID tradeId,
                                                  @Valid @RequestBody P2PReputationDtos.RatingRequest request) {
        return reputationService.rate(currentUserId(authentication), tradeId, request);
    }

    @GetMapping("/me")
    public P2PReputationDtos.ReputationResponse me(Authentication authentication) {
        return reputationService.get(currentUserId(authentication));
    }

    @GetMapping("/{userId}")
    public P2PReputationDtos.ReputationResponse get(@PathVariable UUID userId) {
        return reputationService.get(userId);
    }

    @GetMapping("/{userId}/ratings")
    public List<P2PReputationDtos.RatingResponse> ratings(@PathVariable UUID userId) {
        return reputationService.ratings(userId);
    }

    private UUID currentUserId(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }
}