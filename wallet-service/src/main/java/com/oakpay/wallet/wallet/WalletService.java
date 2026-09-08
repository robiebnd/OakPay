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
        return toResponse(walletRepository.save(newWallet(userId, normalized)));
    }

    @Transactional
    public List<WalletDtos.WalletResponse> getUserWallets(UUID userId) {
        // OakPay provisions the core fiat wallets plus USDT for every user.
        // Blockchain deposit addresses remain a separate custody/address-assignment concern.
        ensureDefaultWallet(userId, "USD");
        ensureDefaultWallet(userId, "ZWG");
        ensureDefaultWallet(userId, "USDT");
        return walletRepository.findAllByUserId(userId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public WalletDtos.WalletResponse getWallet(UUID userId, String currency) {
        Wallet wallet = walletRepository.findByUserIdAndCurrency(userId, normalizeCurrency(currency))
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found"));
        return toResponse(wallet);
    }

    @Transactional(readOnly = true)
    public WalletDtos.BalanceResponse getBalance(UUID userId, String currency) {
        Wallet wallet = walletRepository.findByUserIdAndCurrency(userId, normalizeCurrency(currency))
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found"));
        BigDecimal available = wallet.getAvailableBalance();
        BigDecimal locked = wallet.getLockedBalance();
        BigDecimal total = available.add(locked);
        return new WalletDtos.BalanceResponse(wallet.getId(), wallet.getUserId(), wallet.getCurrency(), available, locked, total);
    }

    @Transactional(readOnly = true)
    public WalletDtos.WalletResponse getWalletById(UUID userId, UUID walletId) {
        Wallet wallet = walletRepository.findByIdAndUserId(walletId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found"));
        return toResponse(wallet);
    }

    private void ensureDefaultWallet(UUID userId, String currency) {
        if (!walletRepository.existsByUserIdAndCurrency(userId, currency)) {
            walletRepository.save(newWallet(userId, currency));
        }
    }

    private Wallet newWallet(UUID userId, String currency) {
        Wallet wallet = new Wallet();
        wallet.setUserId(userId);
        wallet.setCurrency(currency);
        wallet.setAvailableBalance(BigDecimal.ZERO);
        wallet.setLockedBalance(BigDecimal.ZERO);
        return wallet;
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
