package com.oakpay.wallet.wallet;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WalletRepository extends JpaRepository<Wallet, UUID> {
    List<Wallet> findAllByUserId(UUID userId);
    Optional<Wallet> findByUserIdAndCurrency(UUID userId, String currency);
    boolean existsByUserIdAndCurrency(UUID userId, String currency);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select w from Wallet w where w.userId = :userId and w.currency = :currency")
    Optional<Wallet> findByUserIdAndCurrencyForUpdate(@Param("userId") UUID userId,
                                                       @Param("currency") String currency);
}
