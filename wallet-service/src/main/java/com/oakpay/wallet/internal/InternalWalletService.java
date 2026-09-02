package com.oakpay.wallet.internal;

import com.oakpay.wallet.ledger.LedgerEntry;
import com.oakpay.wallet.ledger.LedgerEntryRepository;
import com.oakpay.wallet.ledger.LedgerStatus;
import com.oakpay.wallet.ledger.LedgerTransactionType;
import com.oakpay.wallet.wallet.Wallet;
import com.oakpay.wallet.wallet.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class InternalWalletService {
    private final WalletRepository walletRepository;
    private final LedgerEntryRepository ledgerRepository;

    public InternalWalletService(WalletRepository walletRepository, LedgerEntryRepository ledgerRepository) {
        this.walletRepository = walletRepository;
        this.ledgerRepository = ledgerRepository;
    }

    @Transactional
    public void lock(UUID userId, String currency, InternalWalletDtos.MutationRequest request) {
        Wallet wallet = wallet(userId, currency);
        BigDecimal amount = positive(request.amount());
        if (wallet.getAvailableBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient available balance");
        }
        wallet.setAvailableBalance(wallet.getAvailableBalance().subtract(amount));
        wallet.setLockedBalance(wallet.getLockedBalance().add(amount));
        walletRepository.save(wallet);
    }

    @Transactional
    public void unlock(UUID userId, String currency, InternalWalletDtos.MutationRequest request) {
        Wallet wallet = wallet(userId, currency);
        BigDecimal amount = positive(request.amount());
        if (wallet.getLockedBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient locked balance");
        }
        wallet.setLockedBalance(wallet.getLockedBalance().subtract(amount));
        wallet.setAvailableBalance(wallet.getAvailableBalance().add(amount));
        walletRepository.save(wallet);
    }

    @Transactional
    public void settle(InternalWalletDtos.SettlementRequest request) {
        validateSettlement(request);
        String base = normalize(request.baseCurrency());
        String quote = normalize(request.quoteCurrency());
        BigDecimal baseAmount = positive(request.baseAmount());
        BigDecimal quoteAmount = positive(request.quoteAmount());
        BigDecimal buyerFee = nonNegative(request.buyerFee());
        BigDecimal sellerFee = nonNegative(request.sellerFee());

        Wallet buyerBase = wallet(request.buyerId(), base);
        Wallet buyerQuote = wallet(request.buyerId(), quote);
        Wallet sellerBase = wallet(request.sellerId(), base);
        Wallet sellerQuote = wallet(request.sellerId(), quote);

        if (buyerQuote.getLockedBalance().compareTo(quoteAmount.add(buyerFee)) < 0) {
            throw new IllegalStateException("Buyer locked quote balance is insufficient");
        }
        if (sellerBase.getLockedBalance().compareTo(baseAmount) < 0) {
            throw new IllegalStateException("Seller locked base balance is insufficient");
        }

        buyerQuote.setLockedBalance(buyerQuote.getLockedBalance().subtract(quoteAmount.add(buyerFee)));
        sellerQuote.setAvailableBalance(sellerQuote.getAvailableBalance().add(quoteAmount.subtract(sellerFee)));
        buyerBase.setAvailableBalance(buyerBase.getAvailableBalance().add(baseAmount));
        sellerBase.setLockedBalance(sellerBase.getLockedBalance().subtract(baseAmount));

        walletRepository.save(buyerQuote);
        walletRepository.save(sellerQuote);
        walletRepository.save(buyerBase);
        walletRepository.save(sellerBase);

        String ref = request.reference();
        addLedger(buyerBase, request.buyerId(), LedgerTransactionType.TRADE_BUY, baseAmount, ref + ":BUY", "P2P trade");
        addLedger(sellerBase, request.sellerId(), LedgerTransactionType.TRADE_SELL, baseAmount, ref + ":SELL", "P2P trade");
        addLedger(sellerQuote, request.sellerId(), LedgerTransactionType.TRANSFER_IN, quoteAmount.subtract(sellerFee), ref + ":QUOTE", "P2P trade proceeds");
        if (buyerFee.signum() > 0) {
            addLedger(buyerQuote, request.buyerId(), LedgerTransactionType.FEE, buyerFee, ref + ":BUYER_FEE", "P2P trading fee");
        }
        if (sellerFee.signum() > 0) {
            addLedger(sellerQuote, request.sellerId(), LedgerTransactionType.FEE, sellerFee, ref + ":SELLER_FEE", "P2P trading fee");
        }
    }

    private void addLedger(Wallet wallet, UUID userId, LedgerTransactionType type, BigDecimal amount,
                           String reference, String metadata) {
        if (ledgerRepository.findByReference(reference).isPresent()) return;
        BigDecimal before = wallet.getAvailableBalance();
        LedgerEntry entry = new LedgerEntry();
        entry.setWalletId(wallet.getId());
        entry.setUserId(userId);
        entry.setTransactionType(type);
        entry.setStatus(LedgerStatus.COMPLETED);
        entry.setCurrency(wallet.getCurrency());
        entry.setAmount(amount);
        entry.setBalanceBefore(before.subtract(amount));
        entry.setBalanceAfter(before);
        entry.setReference(reference);
        entry.setMetadata(metadata);
        ledgerRepository.save(entry);
    }

    private Wallet wallet(UUID userId, String currency) {
        return walletRepository.findByUserIdAndCurrencyForUpdate(userId, normalize(currency))
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found for currency " + currency));
    }

    private void validateSettlement(InternalWalletDtos.SettlementRequest r) {
        if (r.buyerId() == null || r.sellerId() == null || r.buyerId().equals(r.sellerId()))
            throw new IllegalArgumentException("Buyer and seller must be different users");
        if (r.reference() == null || r.reference().isBlank()) throw new IllegalArgumentException("Reference is required");
    }

    private BigDecimal positive(BigDecimal value) {
        if (value == null || value.signum() <= 0) throw new IllegalArgumentException("Amount must be greater than zero");
        return value;
    }

    private BigDecimal nonNegative(BigDecimal value) {
        if (value == null || value.signum() < 0) throw new IllegalArgumentException("Fee cannot be negative");
        return value;
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Currency is required");
        return value.trim().toUpperCase();
    }
}
