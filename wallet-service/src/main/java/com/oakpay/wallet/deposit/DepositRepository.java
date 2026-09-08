package com.oakpay.wallet.deposit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DepositRepository extends JpaRepository<Deposit, UUID> {
    Optional<Deposit> findByTxHashAndNetwork(String txHash, String network);
    List<Deposit> findTop50ByUserIdOrderByDetectedAtDesc(UUID userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Deposit> findWithLockByTxHashAndNetwork(String txHash, String network);
}
