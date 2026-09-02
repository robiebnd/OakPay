package com.oakpay.trading.p2p;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public final class P2PReputationDtos {
    private P2PReputationDtos() {}

    public record RatingRequest(
            @Min(1) @Max(5) int score,
            @Size(max = 1000) String comment
    ) {}

    public record RatingResponse(
            UUID id,
            UUID tradeId,
            UUID raterId,
            UUID ratedUserId,
            int score,
            String comment,
            LocalDateTime createdAt
    ) {}

    public record ReputationResponse(
            UUID userId,
            BigDecimal averageRating,
            long totalRatings,
            long fiveStarRatings,
            long fourStarRatings,
            long threeStarRatings,
            long twoStarRatings,
            long oneStarRatings,
            long completedTrades,
            long closedTrades,
            long disputedTrades,
            BigDecimal completionRate,
            BigDecimal disputeRate
    ) {}
}