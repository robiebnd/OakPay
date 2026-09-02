package com.oakpay.trading.p2p;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface P2PRatingRepository extends JpaRepository<P2PRating, UUID> {

    Optional<P2PRating> findByTradeIdAndRaterId(UUID tradeId, UUID raterId);

    List<P2PRating> findAllByRatedUserIdOrderByCreatedAtDesc(UUID ratedUserId);

    @Query("select avg(r.score), count(r), sum(case when r.score = 5 then 1 else 0 end), " +
           "sum(case when r.score = 4 then 1 else 0 end), sum(case when r.score = 3 then 1 else 0 end), " +
           "sum(case when r.score = 2 then 1 else 0 end), sum(case when r.score = 1 then 1 else 0 end) " +
           "from P2PRating r where r.ratedUserId = :userId")
    Object[] aggregateForUser(@Param("userId") UUID userId);
}