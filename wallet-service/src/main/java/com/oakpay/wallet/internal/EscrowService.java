package com.oakpay.wallet.internal;

import com.oakpay.wallet.wallet.Wallet;
import com.oakpay.wallet.wallet.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class EscrowService {
    private final WalletRepository walletRepository;

    public EscrowService(WalletRepository walletRepository) { this.walletRepository = walletRepository; }

    @Transactional
    public void release(UUID sellerId, UUID buyerId, String asset, BigDecimal amount) {
        if (sellerId.equals(buyerId)) throw new IllegalArgumentException("Seller and buyer must differ");
        BigDecimal value = positive(amount);
        Wallet seller = wallet(sellerId, asset);
        Wallet buyer = wallet(buyerId, asset);
        if (seller.getLockedBalance().compareTo(value) < 0)
            throw new IllegalStateException("Seller escrow balance is insufficient");
        seller.setLockedBalance(seller.getLockedBalance().subtract(value));
        buyer.setAvailableBalance(buyer.getAvailableBalance().add(value));
        walletRepository.save(seller);
        walletRepository.save(buyer);
    }

    private Wallet wallet(UUID userId, String currency) {
        return walletRepository.findByUserIdAndCurrencyForUpdate(userId, currency.trim().toUpperCase())
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found for currency " + currency));
    }

    private BigDecimal positive(BigDecimal value) {
        if (value == null || value.signum() <= 0) throw new IllegalArgumentException("Amount must be greater than zero");
        return value;
    }
}
