package com.oakpay.wallet.custody;

import com.oakpay.wallet.ledger.*;
import com.oakpay.wallet.wallet.Wallet;
import com.oakpay.wallet.wallet.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class CustodyWithdrawalService {
    private final WalletRepository walletRepository;
    private final CustodyProviderService custodyProviderService;
    private final LedgerEntryRepository ledgerRepository;

    public CustodyWithdrawalService(WalletRepository walletRepository,
                                     CustodyProviderService custodyProviderService,
                                     LedgerEntryRepository ledgerRepository) {
        this.walletRepository = walletRepository;
        this.custodyProviderService = custodyProviderService;
        this.ledgerRepository = ledgerRepository;
    }

    @Transactional
    public CustodyOperation submit(UUID userId, String currency, String network, BigDecimal amount,
                                   String destinationAddress, String memoTag, String idempotencyKey) {
        String normalizedCurrency = normalize(currency);
        BigDecimal normalizedAmount = positive(amount);
        CustodyOperation existing = custodyProviderService.findWithdrawalByIdempotencyKeyOrNull(idempotencyKey);
        if (existing != null) return existing;

        Wallet wallet = walletRepository.findByUserIdAndCurrencyForUpdate(userId, normalizedCurrency)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found for currency " + normalizedCurrency));

        if (wallet.getAvailableBalance().compareTo(normalizedAmount) < 0) {
            throw new IllegalStateException("Insufficient available balance");
        }

        wallet.setAvailableBalance(wallet.getAvailableBalance().subtract(normalizedAmount));
        wallet.setLockedBalance(wallet.getLockedBalance().add(normalizedAmount));
        walletRepository.save(wallet);

        try {
            CustodyOperation operation = custodyProviderService.submitWithdrawal(userId, normalizedCurrency, network,
                    normalizedAmount, destinationAddress, memoTag, idempotencyKey);
            if (operation.getStatus() == CustodyOperationStatus.COMPLETED) {
                return applyProviderStatus(operation.getProviderName(), operation.getProviderReference(),
                        CustodyProvider.ProviderTransactionStatus.COMPLETED);
            }
            if (operation.getStatus() == CustodyOperationStatus.FAILED) {
                return applyProviderStatus(operation.getProviderName(), operation.getProviderReference(),
                        CustodyProvider.ProviderTransactionStatus.FAILED);
            }
            return operation;
        } catch (CustodySubmissionUnknownException e) {
            // Do not release funds: the provider may have accepted the withdrawal.
            // The operation is persisted as SUBMISSION_UNKNOWN and must be reconciled.
            return custodyProviderService.findWithdrawalByIdempotencyKey(idempotencyKey);
        } catch (RuntimeException e) {
            wallet.setLockedBalance(wallet.getLockedBalance().subtract(normalizedAmount));
            wallet.setAvailableBalance(wallet.getAvailableBalance().add(normalizedAmount));
            walletRepository.save(wallet);
            throw e;
        }
    }

    @Transactional
    public CustodyOperation applyProviderStatus(String provider, String providerReference,
                                                 CustodyProvider.ProviderTransactionStatus status) {
        CustodyOperation operation = custodyProviderService.getByProviderReference(provider, providerReference);
        if (operation.getOperationType() != CustodyOperationType.WITHDRAWAL) {
            throw new IllegalArgumentException("Provider reference is not a withdrawal");
        }

        if (operation.getStatus() == CustodyOperationStatus.COMPLETED
                || operation.getStatus() == CustodyOperationStatus.FAILED) {
            return operation;
        }

        Wallet wallet = walletRepository.findByUserIdAndCurrencyForUpdate(
                operation.getUserId(), operation.getCurrency())
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found for withdrawal"));

        if (status == CustodyProvider.ProviderTransactionStatus.FAILED) {
            releaseLocked(wallet, operation.getAmount());
            operation.setStatus(CustodyOperationStatus.FAILED);
            return operation;
        }

        if (status == CustodyProvider.ProviderTransactionStatus.COMPLETED) {
            if (wallet.getLockedBalance().compareTo(operation.getAmount()) < 0) {
                throw new IllegalStateException("Locked balance is insufficient for completed withdrawal");
            }
            wallet.setLockedBalance(wallet.getLockedBalance().subtract(operation.getAmount()));
            walletRepository.save(wallet);
            createCompletedLedger(operation);
            operation.setStatus(CustodyOperationStatus.COMPLETED);
            return operation;
        }

        operation.setStatus(CustodyOperationStatus.PROCESSING);
        return operation;
    }

    private void releaseLocked(Wallet wallet, BigDecimal amount) {
        if (wallet.getLockedBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("Locked balance is insufficient to release withdrawal");
        }
        wallet.setLockedBalance(wallet.getLockedBalance().subtract(amount));
        wallet.setAvailableBalance(wallet.getAvailableBalance().add(amount));
        walletRepository.save(wallet);
    }

    private void createCompletedLedger(CustodyOperation operation) {
        String reference = "CUSTODY-WITHDRAWAL:" + operation.getProviderName() + ":" + operation.getProviderReference();
        if (ledgerRepository.findByReference(reference).isPresent()) return;

        Wallet wallet = walletRepository.findByUserIdAndCurrencyForUpdate(
                operation.getUserId(), operation.getCurrency())
                .orElseThrow(() -> new IllegalStateException("Wallet not found for withdrawal ledger"));

        BigDecimal before = wallet.getAvailableBalance();
        LedgerEntry entry = new LedgerEntry();
        entry.setWalletId(wallet.getId());
        entry.setUserId(operation.getUserId());
        entry.setTransactionType(LedgerTransactionType.WITHDRAWAL);
        entry.setStatus(LedgerStatus.COMPLETED);
        entry.setDirection(LedgerDirection.DEBIT);
        entry.setBalanceType(LedgerBalanceType.LOCKED);
        entry.setCurrency(operation.getCurrency());
        entry.setAmount(operation.getAmount());
        entry.setBalanceBefore(before);
        entry.setBalanceAfter(before);
        entry.setReference(reference);
        entry.setMetadata("custodyProvider=" + operation.getProviderName()
                + ";providerReference=" + operation.getProviderReference());
        ledgerRepository.save(entry);
    }

    private BigDecimal positive(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) throw new IllegalArgumentException("Amount must be greater than zero");
        return amount;
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Value is required");
        return value.trim().toUpperCase();
    }
}
