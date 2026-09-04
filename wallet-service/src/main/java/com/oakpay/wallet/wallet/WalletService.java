package com.oakpay.wallet.wallet;

import com.oakpay.wallet.api.WalletDtos;
import com.oakpay.wallet.ledger.LedgerBalanceType;
import com.oakpay.wallet.ledger.LedgerDirection;
import com.oakpay.wallet.ledger.LedgerEntry;
import com.oakpay.wallet.ledger.LedgerEntryRepository;
import com.oakpay.wallet.ledger.LedgerStatus;
import com.oakpay.wallet.ledger.LedgerTransactionType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class WalletService {
    private final WalletRepository walletRepository;
    private final LedgerEntryRepository ledgerEntryRepository;

    public WalletService(WalletRepository walletRepository, LedgerEntryRepository ledgerEntryRepository) {
        this.walletRepository = walletRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
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

    @Transactional
    public WalletDtos.WalletResponse deposit(UUID userId, String currency, WalletDtos.DepositRequest request) {
        String normalizedCurrency = normalizeCurrency(currency);
        BigDecimal amount = request.amount();
        String reference = request.reference().trim();

        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Deposit amount must be greater than zero");
        }
        if (reference.isBlank()) {
            throw new IllegalArgumentException("Deposit reference is required");
        }
        if (reference.length() > 100) {
            throw new IllegalArgumentException("Deposit reference must not exceed 100 characters");
        }
        if (ledgerEntryRepository.findByReference(reference).isPresent()) {
            throw new IllegalArgumentException("Deposit reference already exists");
        }

        Wallet wallet = walletRepository.findByUserIdAndCurrencyForUpdate(userId, normalizedCurrency)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found for currency " + normalizedCurrency));

        BigDecimal balanceBefore = wallet.getAvailableBalance();
        BigDecimal balanceAfter = balanceBefore.add(amount);
        wallet.setAvailableBalance(balanceAfter);
        walletRepository.save(wallet);

        LedgerEntry entry = new LedgerEntry();
        entry.setWalletId(wallet.getId());
        entry.setUserId(userId);
        entry.setTransactionType(LedgerTransactionType.DEPOSIT);
        entry.setStatus(LedgerStatus.COMPLETED);
        entry.setDirection(LedgerDirection.CREDIT);
        entry.setBalanceType(LedgerBalanceType.AVAILABLE);
        entry.setCurrency(normalizedCurrency);
        entry.setAmount(amount);
        entry.setBalanceBefore(balanceBefore);
        entry.setBalanceAfter(balanceAfter);
        entry.setReference(reference);
        entry.setMetadata("Development/test deposit");
        ledgerEntryRepository.save(entry);

        return toResponse(wallet);
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
