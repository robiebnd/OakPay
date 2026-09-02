package com.oakpay.wallet.ledger;

import com.oakpay.wallet.api.LedgerDtos;
import com.oakpay.wallet.wallet.Wallet;
import com.oakpay.wallet.wallet.WalletRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class LedgerService {
    private final LedgerEntryRepository ledgerRepository;
    private final WalletRepository walletRepository;

    public LedgerService(LedgerEntryRepository ledgerRepository, WalletRepository walletRepository) {
        this.ledgerRepository = ledgerRepository;
        this.walletRepository = walletRepository;
    }

    @Transactional
    public LedgerDtos.LedgerResponse deposit(UUID userId, String currency, LedgerDtos.BalanceRequest request) {
        return mutate(userId, currency, request, LedgerTransactionType.DEPOSIT);
    }

    @Transactional
    public LedgerDtos.LedgerResponse withdraw(UUID userId, String currency, LedgerDtos.BalanceRequest request) {
        return mutate(userId, currency, request, LedgerTransactionType.WITHDRAWAL);
    }

    @Transactional(readOnly = true)
    public List<LedgerDtos.LedgerResponse> getTransactions(UUID userId, String currency, int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 100);
        PageRequest page = PageRequest.of(0, safeLimit);
        List<LedgerEntry> entries = currency == null || currency.isBlank()
                ? ledgerRepository.findByUserIdOrderByCreatedAtDesc(userId, page)
                : ledgerRepository.findByUserIdAndCurrencyOrderByCreatedAtDesc(userId, normalize(currency), page);
        return entries.stream().map(LedgerDtos.LedgerResponse::from).toList();
    }

    private LedgerDtos.LedgerResponse mutate(UUID userId, String currency, LedgerDtos.BalanceRequest request,
                                              LedgerTransactionType type) {
        String normalizedCurrency = normalize(currency);
        String reference = request.reference().trim();
        if (reference.isBlank()) throw new IllegalArgumentException("Reference is required");

        var existing = ledgerRepository.findByReference(reference);
        if (existing.isPresent()) {
            LedgerEntry entry = existing.get();
            if (!entry.getUserId().equals(userId)) throw new IllegalArgumentException("Reference already belongs to another user");
            if (entry.getTransactionType() != type || !entry.getCurrency().equals(normalizedCurrency)
                    || entry.getAmount().compareTo(request.amount()) != 0) {
                throw new IllegalArgumentException("Reference was already used for a different transaction");
            }
            return LedgerDtos.LedgerResponse.from(entry);
        }

        BigDecimal amount = request.amount();
        if (amount == null || amount.signum() <= 0) throw new IllegalArgumentException("Amount must be greater than zero");

        Wallet wallet = walletRepository.findByUserIdAndCurrencyForUpdate(userId, normalizedCurrency)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found for currency " + normalizedCurrency));

        BigDecimal before = wallet.getAvailableBalance();
        BigDecimal after;
        if (type == LedgerTransactionType.WITHDRAWAL) {
            if (before.compareTo(amount) < 0) throw new IllegalStateException("Insufficient available balance");
            after = before.subtract(amount);
        } else {
            after = before.add(amount);
        }

        wallet.setAvailableBalance(after);
        walletRepository.save(wallet);

        LedgerEntry entry = new LedgerEntry();
        entry.setWalletId(wallet.getId());
        entry.setUserId(userId);
        entry.setTransactionType(type);
        entry.setStatus(LedgerStatus.COMPLETED);
        entry.setCurrency(normalizedCurrency);
        entry.setAmount(amount);
        entry.setBalanceBefore(before);
        entry.setBalanceAfter(after);
        entry.setReference(reference);
        entry.setMetadata(request.metadata());

        return LedgerDtos.LedgerResponse.from(ledgerRepository.save(entry));
    }

    private String normalize(String currency) {
        if (currency == null || currency.isBlank()) throw new IllegalArgumentException("Currency is required");
        return currency.trim().toUpperCase();
    }
}
