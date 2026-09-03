package com.oakpay.trading.p2p;

import com.oakpay.trading.wallet.WalletClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class P2PTradeService {
    private final P2PTradeRepository repository;
    private final WalletClient walletClient;
    private final P2PPaymentService paymentService;
    private final P2PPaymentRepository paymentRepository;
    private final P2PCommissionService commissionService;

    public P2PTradeService(P2PTradeRepository repository, WalletClient walletClient,
                           P2PPaymentService paymentService, P2PPaymentRepository paymentRepository,
                           P2PCommissionService commissionService) {
        this.repository = repository;
        this.walletClient = walletClient;
        this.paymentService = paymentService;
        this.paymentRepository = paymentRepository;
        this.commissionService = commissionService;
    }

    @Transactional
    public P2PTradeDtos.TradeResponse create(UUID sellerId, P2PTradeDtos.CreateRequest request) {
        if (request.buyerId() == null || request.buyerId().equals(sellerId))
            throw new IllegalArgumentException("A different buyer is required");
        BigDecimal quantity = positive(request.quantity(), "Quantity");
        BigDecimal price = positive(request.unitPrice(), "Unit price");
        if (request.paymentMethod() == null || request.paymentMethod().isBlank())
            throw new IllegalArgumentException("Payment method is required");
        String asset = normalize(request.asset());
        String fiat = normalize(request.fiatCurrency());
        BigDecimal fiatAmount = quantity.multiply(price).setScale(18, RoundingMode.DOWN);
        int expiry = request.expiryMinutes() == null ? 30 : request.expiryMinutes();
        if (expiry < 5 || expiry > 1440) throw new IllegalArgumentException("Expiry must be between 5 and 1440 minutes");

        P2PTrade trade = new P2PTrade();
        trade.setSellerId(sellerId);
        trade.setBuyerId(request.buyerId());
        trade.setAsset(asset);
        trade.setFiatCurrency(fiat);
        trade.setQuantity(quantity);
        trade.setUnitPrice(price);
        trade.setFiatAmount(fiatAmount);
        trade.setPaymentMethod(request.paymentMethod().trim());
        trade.setExpiresAt(LocalDateTime.now().plusMinutes(expiry));
        trade = repository.save(trade);

        // The seller's crypto is the escrowed asset. The commission is assessed
        // against the fiat transaction value and remains separately auditable.
        commissionService.assess(trade);
        walletClient.lock(sellerId, asset, quantity, trade.getId());
        trade.setStatus(P2PTradeStatus.PAYMENT_PENDING);
        return P2PTradeDtos.TradeResponse.from(repository.save(trade));
    }

    @Transactional
    public P2PTradeDtos.TradeResponse markPaid(UUID buyerId, UUID tradeId, P2PTradeDtos.PaymentRequest request) {
        paymentService.submit(buyerId, tradeId,
                new P2PPaymentDtos.SubmitRequest(request == null ? null : request.paymentReference(),
                        request == null ? null : request.paymentNote()));
        return P2PTradeDtos.TradeResponse.from(get(tradeId));
    }

    @Transactional
    public P2PTradeDtos.TradeResponse confirmPayment(UUID sellerId, UUID tradeId) {
        P2PTrade trade = get(tradeId);
        ensureNotExpired(trade);
        if (!trade.getSellerId().equals(sellerId)) throw new IllegalArgumentException("Trade does not belong to seller");
        if (trade.getStatus() != P2PTradeStatus.PAYMENT_MARKED)
            throw new IllegalStateException("Buyer has not marked payment");

        P2PPayment payment = paymentRepository.findByTradeId(tradeId)
                .orElseThrow(() -> new IllegalStateException("Payment record not found"));
        if (payment.getStatus() != PaymentStatus.VERIFIED)
            throw new IllegalStateException("Payment must be verified before crypto is released");

        // Crypto transfer is kept exact: buyer receives the advertised quantity.
        // OakPay commission is separately assessed on the fiat transaction value.
        walletClient.releaseEscrow(sellerId, trade.getBuyerId(), trade.getAsset(), trade.getQuantity(), trade.getId());
        trade.setStatus(P2PTradeStatus.COMPLETED);
        return P2PTradeDtos.TradeResponse.from(repository.save(trade));
    }

    @Transactional
    public P2PTradeDtos.TradeResponse cancel(UUID userId, UUID tradeId) {
        P2PTrade trade = get(tradeId);
        if (!trade.getBuyerId().equals(userId) && !trade.getSellerId().equals(userId))
            throw new IllegalArgumentException("Trade does not belong to user");
        if (trade.getStatus() == P2PTradeStatus.COMPLETED || trade.getStatus() == P2PTradeStatus.CANCELLED)
            throw new IllegalStateException("Trade cannot be cancelled");
        if (trade.getStatus() == P2PTradeStatus.PAYMENT_MARKED)
            throw new IllegalStateException("Paid trade must be disputed, not cancelled");
        walletClient.unlock(trade.getSellerId(), trade.getAsset(), trade.getQuantity(), trade.getId());
        trade.setStatus(P2PTradeStatus.CANCELLED);
        return P2PTradeDtos.TradeResponse.from(repository.save(trade));
    }

    @Transactional
    public P2PTradeDtos.TradeResponse dispute(UUID userId, UUID tradeId) {
        P2PTrade trade = get(tradeId);
        if (!trade.getBuyerId().equals(userId) && !trade.getSellerId().equals(userId))
            throw new IllegalArgumentException("Trade does not belong to user");
        if (trade.getStatus() != P2PTradeStatus.PAYMENT_MARKED)
            throw new IllegalStateException("Only a payment-marked trade can be disputed");
        trade.setStatus(P2PTradeStatus.DISPUTED);
        return P2PTradeDtos.TradeResponse.from(repository.save(trade));
    }

    @Transactional(readOnly = true)
    public P2PTradeDtos.TradeResponse getOne(UUID userId, UUID tradeId) {
        P2PTrade trade = get(tradeId);
        if (!trade.getBuyerId().equals(userId) && !trade.getSellerId().equals(userId))
            throw new IllegalArgumentException("Trade does not belong to user");
        return P2PTradeDtos.TradeResponse.from(trade);
    }

    @Transactional(readOnly = true)
    public List<P2PTradeDtos.TradeResponse> mine(UUID userId) {
        return repository.findAllByBuyerIdOrSellerIdOrderByCreatedAtDesc(userId, userId).stream()
                .map(P2PTradeDtos.TradeResponse::from).toList();
    }

    private P2PTrade get(UUID id) {
        return repository.findById(id).orElseThrow(() -> new IllegalArgumentException("P2P trade not found"));
    }

    private void ensureNotExpired(P2PTrade trade) {
        if (trade.getExpiresAt().isBefore(LocalDateTime.now())) {
            if (trade.getStatus() == P2PTradeStatus.PAYMENT_PENDING) {
                walletClient.unlock(trade.getSellerId(), trade.getAsset(), trade.getQuantity(), trade.getId());
                trade.setStatus(P2PTradeStatus.EXPIRED);
                repository.save(trade);
            }
            throw new IllegalStateException("P2P trade has expired");
        }
    }

    private BigDecimal positive(BigDecimal value, String label) {
        if (value == null || value.signum() <= 0) throw new IllegalArgumentException(label + " must be greater than zero");
        return value.setScale(18, RoundingMode.DOWN);
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Currency is required");
        return value.trim().toUpperCase();
    }
}
