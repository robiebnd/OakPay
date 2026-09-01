package com.oakpay.wallet.wallet;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WalletRepository extends JpaRepository<Wallet, UUID> {
    List<Wallet> findAllByUserId(UUID userId);
    Optional<Wallet> findByUserIdAndCurrency(UUID userId, String currency);
    boolean existsByUserIdAndCurrency(UUID userId, String currency);
}
