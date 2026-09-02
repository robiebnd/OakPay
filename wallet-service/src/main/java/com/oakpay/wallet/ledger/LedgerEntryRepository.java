package com.oakpay.wallet.ledger;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, UUID> {
    Optional<LedgerEntry> findByReference(String reference);
    List<LedgerEntry> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
    List<LedgerEntry> findByUserIdAndCurrencyOrderByCreatedAtDesc(UUID userId, String currency, Pageable pageable);
    List<LedgerEntry> findByWalletIdAndUserIdOrderByCreatedAtDesc(UUID walletId, UUID userId, Pageable pageable);
}
