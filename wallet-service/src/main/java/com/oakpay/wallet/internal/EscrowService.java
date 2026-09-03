package com.oakpay.wallet.internal;

import com.oakpay.wallet.ledger.LedgerBalanceType;
import com.oakpay.wallet.ledger.LedgerDirection;
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
public class EscrowService {
    private final WalletRepository walletRepository;
    private final LedgerEntryRepository ledgerRepository;

    public EscrowService(WalletRepository walletRepository, LedgerEntryRepository ledgerRepository) {
        this.walletRepository = walletRepository;
        this.ledgerRepository = ledgerRepository;
    }

    @Transactional
    public void release(UUID sellerId, UUID buyerId, String asset, BigDecimal amount, String tradeReference) {
        if (sellerId.equals(buyerId)) throw new IllegalArgumentException("Seller and buyer must differ");
        BigDecimal value = positive(amount);
        String reference = normalizeReference(tradeReference);

        String sellerLedgerReference = "P2P_ESCROW_RELEASE:" + reference + ":SELLER";
        String buyerLedgerReference = "P2P_ESCROW_RELEASE:" + reference + ":BUYER";

        if (ledgerRepository.findByReference(sellerLedgerReference).isPresent()
                && ledgerRepository.findByReference(buyerLedgerReference).isPresent()) {
            return;
        }

        Wallet seller = wallet(sellerId, asset);
        Wallet buyer = wallet(buyerId, asset);
        if (seller.getLockedBalance().compareTo(value) < 0)
            throw new IllegalStateException("Seller escrow balance is insufficient");

        BigDecimal sellerBefore = seller.getLockedBalance();
        BigDecimal buyerBefore = buyer.getAvailableBalance();

        seller.setLockedBalance(sellerBefore.subtract(value));
        buyer.setAvailableBalance(buyerBefore.add(value));
        walletRepository.save(seller);
        walletRepository.save(buyer);

        saveEntry(seller, sellerId, asset, value, sellerBefore, seller.getLockedBalance(),
                sellerLedgerReference, LedgerDirection.DEBIT, LedgerBalanceType.LOCKED,
                "P2P escrow release; trade=" + reference);
        saveEntry(buyer, buyerId, asset, value, buyerBefore, buyer.getAvailableBalance(),
                buyerLedgerReference, LedgerDirection.CREDIT, LedgerBalanceType.AVAILABLE,
                "P2P escrow release; trade=" + reference);
    }

    private void saveEntry(Wallet wallet, UUID userId, String currency, BigDecimal amount,
                           BigDecimal before, BigDecimal after, String reference,
                           LedgerDirection direction, LedgerBalanceType balanceType, String metadata) {
        LedgerEntry entry = new LedgerEntry();
        entry.setWalletId(wallet.getId());
        entry.setUserId(userId);
        entry.setTransactionType(direction == LedgerDirection.CREDIT
                ? LedgerTransactionType.TRADE_BUY : LedgerTransactionType.TRADE_SELL);
        entry.setStatus(LedgerStatus.COMPLETED);
        entry.setDirection(direction);
        entry.setBalanceType(balanceType);
        entry.setCurrency(currency.trim().toUpperCase());
        entry.setAmount(amount);
        entry.setBalanceBefore(before);
        entry.setBalanceAfter(after);
        entry.setReference(reference);
        entry.setMetadata(metadata);
        ledgerRepository.save(entry);
    }

    private Wallet wallet(UUID userId, String currency) {
        return walletRepository.findByUserIdAndCurrencyForUpdate(userId, currency.trim().toUpperCase())
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found for currency " + currency));
    }

    private BigDecimal positive(BigDecimal value) {
        if (value == null || value.signum() <= 0) throw new IllegalArgumentException("Amount must be greater than zero");
        return value;
    }

    private String normalizeReference(String reference) {
        if (reference == null || reference.isBlank()) throw new IllegalArgumentException("Trade reference is required");
        return reference.trim();
    }
}
