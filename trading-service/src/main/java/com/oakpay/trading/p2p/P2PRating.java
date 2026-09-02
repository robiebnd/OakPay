package com.oakpay.trading.p2p;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "p2p_ratings",
        uniqueConstraints = @UniqueConstraint(name = "uk_p2p_rating_trade_rater", columnNames = {"trade_id", "rater_id"}),
        indexes = {
                @Index(name = "idx_p2p_rating_rated_user", columnList = "rated_user_id"),
                @Index(name = "idx_p2p_rating_created_at", columnList = "created_at")
        })
public class P2PRating {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "trade_id", nullable = false)
    private UUID tradeId;

    @Column(name = "rater_id", nullable = false)
    private UUID raterId;

    @Column(name = "rated_user_id", nullable = false)
    private UUID ratedUserId;

    @Column(nullable = false)
    private Integer score;

    @Column(length = 1000)
    private String comment;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public UUID getTradeId() { return tradeId; }
    public void setTradeId(UUID tradeId) { this.tradeId = tradeId; }
    public UUID getRaterId() { return raterId; }
    public void setRaterId(UUID raterId) { this.raterId = raterId; }
    public UUID getRatedUserId() { return ratedUserId; }
    public void setRatedUserId(UUID ratedUserId) { this.ratedUserId = ratedUserId; }
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}