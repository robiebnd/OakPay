package com.oakpay.trading.p2p;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Service
public class P2PReputationService {

    private final P2PRatingRepository ratingRepository;
    private final P2PTradeRepository tradeRepository;

    public P2PReputationService(P2PRatingRepository ratingRepository, P2PTradeRepository tradeRepository) {
        this.ratingRepository = ratingRepository;
        this.tradeRepository = tradeRepository;
    }

    @Transactional
    public P2PReputationDtos.RatingResponse rate(UUID raterId, UUID tradeId, P2PReputationDtos.RatingRequest request) {
        P2PTrade trade = tradeRepository.findById(tradeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Trade not found"));

        if (trade.getStatus() != P2PTradeStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only completed trades can be rated");
        }
        if (!raterId.equals(trade.getBuyerId()) && !raterId.equals(trade.getSellerId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not a participant in this trade");
        }

        UUID ratedUserId = raterId.equals(trade.getBuyerId()) ? trade.getSellerId() : trade.getBuyerId();
        if (ratingRepository.findByTradeIdAndRaterId(tradeId, raterId).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You have already rated this trade");
        }

        P2PRating rating = new P2PRating();
        rating.setTradeId(tradeId);
        rating.setRaterId(raterId);
        rating.setRatedUserId(ratedUserId);
        rating.setScore(request.score());
        rating.setComment(request.comment() == null ? null : request.comment().trim());

        return toResponse(ratingRepository.save(rating));
    }

    @Transactional(readOnly = true)
    public P2PReputationDtos.ReputationResponse get(UUID userId) {
        Object[] aggregate = ratingRepository.aggregateForUser(userId);
        Object[] values = aggregate == null ? null : aggregate;

        BigDecimal average = values != null && values[0] != null
                ? ((Number) values[0]).doubleValue() == 0 ? BigDecimal.ZERO : BigDecimal.valueOf(((Number) values[0]).doubleValue()).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        long totalRatings = number(values, 1);
        long five = number(values, 2);
        long four = number(values, 3);
        long three = number(values, 4);
        long two = number(values, 5);
        long one = number(values, 6);

        List<P2PTrade> trades = tradeRepository.findAllByBuyerIdOrSellerIdOrderByCreatedAtDesc(userId, userId);
        long completed = trades.stream().filter(t -> t.getStatus() == P2PTradeStatus.COMPLETED).count();
        long closed = trades.stream().filter(t -> t.getStatus() == P2PTradeStatus.COMPLETED ||
                t.getStatus() == P2PTradeStatus.CANCELLED || t.getStatus() == P2PTradeStatus.EXPIRED).count();
        long disputed = trades.stream().filter(t -> t.getStatus() == P2PTradeStatus.DISPUTED).count();

        BigDecimal completionRate = closed == 0 ? BigDecimal.ZERO : percentage(completed, closed);
        BigDecimal disputeRate = trades.isEmpty() ? BigDecimal.ZERO : percentage(disputed, trades.size());

        return new P2PReputationDtos.ReputationResponse(userId, average, totalRatings, five, four, three, two, one,
                completed, closed, disputed, completionRate, disputeRate);
    }

    @Transactional(readOnly = true)
    public List<P2PReputationDtos.RatingResponse> ratings(UUID userId) {
        return ratingRepository.findAllByRatedUserIdOrderByCreatedAtDesc(userId).stream().map(this::toResponse).toList();
    }

    private long number(Object[] values, int index) {
        if (values == null || values.length <= index || values[index] == null) return 0;
        return ((Number) values[index]).longValue();
    }

    private BigDecimal percentage(long numerator, long denominator) {
        return BigDecimal.valueOf(numerator * 100.0 / denominator).setScale(2, RoundingMode.HALF_UP);
    }

    private P2PReputationDtos.RatingResponse toResponse(P2PRating r) {
        return new P2PReputationDtos.RatingResponse(r.getId(), r.getTradeId(), r.getRaterId(), r.getRatedUserId(),
                r.getScore(), r.getComment(), r.getCreatedAt());
    }
}