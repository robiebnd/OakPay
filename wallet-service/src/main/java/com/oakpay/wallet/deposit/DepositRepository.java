package com.oakpay.wallet.deposit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.Lock;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DepositRepository extends JpaRepository<Deposit, UUID> {
    Optional<Deposit> findByTxHashAndNetwork(String txHash, String network);
    List<Deposit> findTop50ByUserIdOrderByDetectedAtDesc(UUID userId);

    @Modifying
    @Query(value = """
            INSERT INTO deposits (id,user_id,deposit_address_id,currency,network,address,tx_hash,amount,confirmations,required_confirmations,status,failure_reason,detected_at,updated_at)
            VALUES (:id,:userId,:addressId,:currency,:network,:address,:txHash,:amount,:confirmations,:required,:status,NULL,:detectedAt,:updatedAt)
            ON CONFLICT (tx_hash,network) DO NOTHING
            """, nativeQuery = true)
    int insertIfAbsent(@Param("id") UUID id,@Param("userId") UUID userId,@Param("addressId") UUID addressId,
                       @Param("currency") String currency,@Param("network") String network,@Param("address") String address,
                       @Param("txHash") String txHash,@Param("amount") BigDecimal amount,@Param("confirmations") int confirmations,
                       @Param("required") int required,@Param("status") String status,@Param("detectedAt") LocalDateTime detectedAt,
                       @Param("updatedAt") LocalDateTime updatedAt);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Deposit> findWithLockByTxHashAndNetwork(String txHash, String network);
}
