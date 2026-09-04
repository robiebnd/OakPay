package com.oakpay.wallet.wallet;

import com.oakpay.wallet.api.WalletDtos;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class WalletService {
    private final WalletRepository walletRepository;

    public WalletService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    @Transactional
    public WalletDtos.WalletResponse createWallet(UUID userId, String currency) {
        String normalized = normalizeCurrency(currency);
        if (walletRepository.existsByUserIdAndCurrency(userId, normalized)) {
            throw new IllegalArgumentException("Wallet already exists for currency " + normalized);
        }
        Wallet wallet = new Wallet();
        wallet.setUserId(userId);
        wallet.setCurrency(normalized);
        wallet.setAvailableBalance(BigDecimal.ZERO);
        wallet.setLockedBalance(BigDecimal.ZERO);
        return toResponse(walletRepository.save(wallet));
    }

    @Transactional(readOnly = true)
    public List<WalletDtos.WalletResponse> getUserWallets(UUID userId) {
        return walletRepository.findAllByUserId(userId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public WalletDtos.WalletResponse getWallet(UUID userId, String currency) {
        Wallet wallet = walletRepository.findByUserIdAndCurrency(userId, normalizeCurrency(currency))
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found"));
        return toResponse(wallet);
    }

    private String normalizeCurrency(String currency) {
        if (currency == null || currency.isBlank()) throw new IllegalArgumentException("Currency is required");
        return currency.trim().toUpperCase();
    }

    private WalletDtos.WalletResponse toResponse(Wallet wallet) {
        BigDecimal total = wallet.getAvailableBalance().add(wallet.getLockedBalance());
        return new WalletDtos.WalletResponse(wallet.getId(), wallet.getUserId(), wallet.getCurrency(),
                wallet.getAvailableBalance(), wallet.getLockedBalance(), total,
                wallet.getCreatedAt(), wallet.getUpdatedAt());
    }
}
