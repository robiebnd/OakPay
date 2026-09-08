package com.oakpay.wallet.ledger;

import com.oakpay.wallet.api.LedgerDtos;
import com.oakpay.wallet.wallet.Wallet;
import com.oakpay.wallet.wallet.WalletRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

    /** Credits a confirmed blockchain deposit. The reference makes the credit idempotent. */
    @Transactional
    public LedgerDtos.LedgerResponse creditExternalDeposit(UUID userId, String currency, BigDecimal amount,
                                                             String reference, String metadata) {
        String normalizedCurrency = normalize(currency);
        if (amount == null || amount.signum() <= 0) throw new IllegalArgumentException("Amount must be greater than zero");
        BigDecimal normalizedAmount = amount.setScale(Wallet.scaleFor(normalizedCurrency), RoundingMode.HALF_UP);
        if (reference == null || reference.isBlank()) throw new IllegalArgumentException("Reference is required");
        String normalizedReference = reference.trim();

        var existing = ledgerRepository.findByReference(normalizedReference);
        if (existing.isPresent()) {
            LedgerEntry entry = existing.get();
            if (!entry.getUserId().equals(userId)
                    || entry.getTransactionType() != LedgerTransactionType.DEPOSIT
                    || !entry.getCurrency().equals(normalizedCurrency)
                    || entry.getAmount().compareTo(normalizedAmount) != 0) {
                throw new IllegalArgumentException("Reference was already used for a different deposit");
            }
            return LedgerDtos.LedgerResponse.from(entry);
        }

        Wallet wallet = walletRepository.findByUserIdAndCurrencyForUpdate(userId, normalizedCurrency)
                .orElseGet(() -> createWalletForDeposit(userId, normalizedCurrency));
        BigDecimal before = wallet.getAvailableBalance();
        BigDecimal after = before.add(normalizedAmount);
        wallet.setAvailableBalance(after);
        walletRepository.save(wallet);

        LedgerEntry entry = new LedgerEntry();
        entry.setWalletId(wallet.getId());
        entry.setUserId(userId);
        entry.setTransactionType(LedgerTransactionType.DEPOSIT);
        entry.setStatus(LedgerStatus.COMPLETED);
        entry.setDirection(LedgerDirection.CREDIT);
        entry.setBalanceType(LedgerBalanceType.AVAILABLE);
        entry.setCurrency(normalizedCurrency);
        entry.setAmount(normalizedAmount);
        entry.setBalanceBefore(before);
        entry.setBalanceAfter(after);
        entry.setReference(normalizedReference);
        entry.setMetadata(metadata);
        return LedgerDtos.LedgerResponse.from(ledgerRepository.save(entry));
    }

    @Transactional(readOnly = true)
    public List<LedgerDtos.LedgerResponse> getTransactions(UUID userId, String currency, int limit) {
        int safe = Math.min(Math.max(limit, 1), 100);
        PageRequest page = PageRequest.of(0, safe);
        List<LedgerEntry> entries = currency == null || currency.isBlank()
                ? ledgerRepository.findByUserIdOrderByCreatedAtDesc(userId, page)
                : ledgerRepository.findByUserIdAndCurrencyOrderByCreatedAtDesc(userId, normalize(currency), page);
        return entries.stream().map(LedgerDtos.LedgerResponse::from).toList();
    }

    private LedgerDtos.LedgerResponse mutate(UUID userId, String currency, LedgerDtos.BalanceRequest request,
                                              LedgerTransactionType type) {
        if (request == null) throw new IllegalArgumentException("Ledger request is required");
        String normalizedCurrency = normalize(currency);
        BigDecimal amount = request.amount();
        if (amount == null || amount.signum() <= 0) throw new IllegalArgumentException("Amount must be greater than zero");
        amount = amount.setScale(Wallet.scaleFor(normalizedCurrency), RoundingMode.HALF_UP);

        if (request.reference() == null || request.reference().isBlank()) throw new IllegalArgumentException("Reference is required");
        String reference = request.reference().trim();
        var existing = ledgerRepository.findByReference(reference);
        if (existing.isPresent()) {
            LedgerEntry entry = existing.get();
            if (!entry.getUserId().equals(userId)) throw new IllegalArgumentException("Reference already belongs to another user");
            if (entry.getTransactionType() != type || !entry.getCurrency().equals(normalizedCurrency)
                    || entry.getAmount().compareTo(amount) != 0) {
                throw new IllegalArgumentException("Reference was already used for a different transaction");
            }
            return LedgerDtos.LedgerResponse.from(entry);
        }

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
        entry.setDirection(type == LedgerTransactionType.WITHDRAWAL ? LedgerDirection.DEBIT : LedgerDirection.CREDIT);
        entry.setBalanceType(LedgerBalanceType.AVAILABLE);
        entry.setCurrency(normalizedCurrency);
        entry.setAmount(amount);
        entry.setBalanceBefore(before);
        entry.setBalanceAfter(after);
        entry.setReference(reference);
        entry.setMetadata(request.metadata());
        return LedgerDtos.LedgerResponse.from(ledgerRepository.save(entry));
    }

    private Wallet createWalletForDeposit(UUID userId, String currency) {
        Wallet wallet = new Wallet();
        wallet.setUserId(userId);
        wallet.setCurrency(currency);
        wallet.setAvailableBalance(BigDecimal.ZERO.setScale(Wallet.scaleFor(currency)));
        wallet.setLockedBalance(BigDecimal.ZERO.setScale(Wallet.scaleFor(currency)));
        return walletRepository.saveAndFlush(wallet);
    }

    private String normalize(String currency) {
        if (currency == null || currency.isBlank()) throw new IllegalArgumentException("Currency is required");
        return currency.trim().toUpperCase();
    }
}
