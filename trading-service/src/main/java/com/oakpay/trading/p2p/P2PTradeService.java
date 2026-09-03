package com.oakpay.trading.p2p;

import com.oakpay.trading.asset.AssetStatus;
import com.oakpay.trading.asset.SupportedAsset;
import com.oakpay.trading.asset.SupportedAssetRepository;
import com.oakpay.trading.wallet.WalletClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class P2PTradeService {
    private final P2PTradeRepository repository;
    private final WalletClient walletClient;
    private final P2PPaymentService paymentService;
    private final P2PPaymentRepository paymentRepository;
    private final P2PCommissionService commissionService;
    private final SupportedAssetRepository assetRepository;
    private final AdvertisementRepository advertisementRepository;

    public P2PTradeService(P2PTradeRepository repository, WalletClient walletClient,
                           P2PPaymentService paymentService, P2PPaymentRepository paymentRepository,
                           P2PCommissionService commissionService, SupportedAssetRepository assetRepository,
                           AdvertisementRepository advertisementRepository) {
        this.repository = repository;
        this.walletClient = walletClient;
        this.paymentService = paymentService;
        this.paymentRepository = paymentRepository;
        this.commissionService = commissionService;
        this.assetRepository = assetRepository;
        this.advertisementRepository = advertisementRepository;
    }

    @Transactional
    public P2PTradeDtos.TradeResponse create(UUID sellerId, P2PTradeDtos.CreateRequest request) {
        if (request == null) throw new IllegalArgumentException("Trade request is required");
        if (request.buyerId() == null || request.buyerId().equals(sellerId))
            throw new IllegalArgumentException("A different buyer is required");
        BigDecimal quantity = positive(request.quantity(), "Quantity");
        BigDecimal price = positive(request.unitPrice(), "Unit price");
        if (request.paymentMethod() == null || request.paymentMethod().isBlank())
            throw new IllegalArgumentException("Payment method is required");
        String asset = normalize(request.asset());
        String fiat = normalize(request.fiatCurrency());
        String paymentMethod = request.paymentMethod().trim().toUpperCase(Locale.ROOT);
        validateP2PAsset(asset, quantity);
        BigDecimal fiatAmount = quantity.multiply(price).setScale(18, RoundingMode.DOWN);
        int expiry = request.expiryMinutes() == null ? 30 : request.expiryMinutes();
        if (expiry < 5 || expiry > 1440) throw new IllegalArgumentException("Expiry must be between 5 and 1440 minutes");

        Advertisement ad = null;
        if (request.advertisementId() != null) {
            ad = advertisementRepository.findByIdForUpdate(request.advertisementId())
                    .orElseThrow(() -> new IllegalArgumentException("Advertisement not found"));
            UUID expectedOwner = ad.getSide() == OrderSide.SELL ? sellerId : request.buyerId();
            if (!ad.getOwnerId().equals(expectedOwner))
                throw new IllegalArgumentException("Trade participants do not match advertisement");
            if (ad.getStatus() != AdStatus.ACTIVE)
                throw new IllegalStateException("Advertisement is not active");
            if (!ad.getAsset().equals(asset) || !ad.getFiatCurrency().equals(fiat)
                    || ad.getPrice().compareTo(price) != 0)
                throw new IllegalArgumentException("Trade does not match advertisement terms");
            if (quantity.compareTo(ad.getMinQuantity()) < 0 || quantity.compareTo(ad.getMaxQuantity()) > 0
                    || quantity.compareTo(ad.getAvailableQuantity()) > 0)
                throw new IllegalArgumentException("Quantity is outside the advertisement limits");
            if (!containsPaymentMethod(ad.getPaymentMethods(), paymentMethod))
                throw new IllegalArgumentException("Selected payment method is not supported by this advertisement");
        }

        P2PTrade trade = new P2PTrade();
        trade.setSellerId(sellerId);
        trade.setBuyerId(request.buyerId());
        trade.setAdvertisementId(request.advertisementId());
        trade.setAsset(asset);
        trade.setFiatCurrency(fiat);
        trade.setQuantity(quantity);
        trade.setUnitPrice(price);
        trade.setFiatAmount(fiatAmount);
        trade.setPaymentMethod(paymentMethod);
        trade.setExpiresAt(LocalDateTime.now().plusMinutes(expiry));
        trade = repository.save(trade);

        commissionService.assess(trade);
        walletClient.lock(sellerId, asset, quantity, trade.getId());
        trade.setStatus(P2PTradeStatus.PAYMENT_PENDING);

        if (ad != null) {
            ad.setAvailableQuantity(ad.getAvailableQuantity().subtract(quantity));
            if (ad.getAvailableQuantity().signum() == 0) ad.setStatus(AdStatus.CLOSED);
            advertisementRepository.save(ad);
        }
        return P2PTradeDtos.TradeResponse.from(repository.save(trade));
    }

    @Transactional
    public P2PTradeDtos.TradeResponse markPaid(UUID buyerId, UUID tradeId, P2PTradeDtos.PaymentRequest request) {
        P2PTrade trade = get(tradeId);
        if (!trade.getBuyerId().equals(buyerId)) throw new IllegalArgumentException("Trade does not belong to buyer");
        if (trade.getStatus() == P2PTradeStatus.PAYMENT_MARKED) return P2PTradeDtos.TradeResponse.from(trade);
        if (trade.getStatus() != P2PTradeStatus.PAYMENT_PENDING)
            throw new IllegalStateException("Trade is not awaiting payment");
        paymentService.submit(buyerId, tradeId,
                new P2PPaymentDtos.SubmitRequest(request == null ? null : request.paymentReference(),
                        request == null ? null : request.paymentNote()));
        return P2PTradeDtos.TradeResponse.from(get(tradeId));
    }

    @Transactional
    public P2PTradeDtos.TradeResponse confirmPayment(UUID sellerId, UUID tradeId) {
        P2PTrade trade = get(tradeId);
        if (!trade.getSellerId().equals(sellerId)) throw new IllegalArgumentException("Trade does not belong to seller");
        if (trade.getStatus() == P2PTradeStatus.COMPLETED) return P2PTradeDtos.TradeResponse.from(trade);
        ensureNotExpired(trade);
        if (trade.getStatus() != P2PTradeStatus.PAYMENT_MARKED)
            throw new IllegalStateException("Buyer has not marked payment");
        P2PPayment payment = paymentRepository.findByTradeId(tradeId)
                .orElseThrow(() -> new IllegalStateException("Payment record not found"));
        if (payment.getStatus() != PaymentStatus.VERIFIED)
            throw new IllegalStateException("Payment must be verified before crypto is released");
        walletClient.releaseEscrow(sellerId, trade.getBuyerId(), trade.getAsset(), trade.getQuantity(), trade.getId());
        trade.setStatus(P2PTradeStatus.COMPLETED);
        return P2PTradeDtos.TradeResponse.from(repository.save(trade));
    }

    @Transactional
    public P2PTradeDtos.TradeResponse cancel(UUID userId, UUID tradeId) {
        P2PTrade trade = get(tradeId);
        if (!trade.getBuyerId().equals(userId) && !trade.getSellerId().equals(userId))
            throw new IllegalArgumentException("Trade does not belong to user");
        if (trade.getStatus() == P2PTradeStatus.COMPLETED)
            throw new IllegalStateException("Completed trade cannot be cancelled");
        if (trade.getStatus() == P2PTradeStatus.CANCELLED || trade.getStatus() == P2PTradeStatus.EXPIRED)
            return P2PTradeDtos.TradeResponse.from(trade);
        if (trade.getStatus() == P2PTradeStatus.PAYMENT_MARKED || trade.getStatus() == P2PTradeStatus.DISPUTED)
            throw new IllegalStateException("Paid or disputed trade must not be cancelled");
        walletClient.unlock(trade.getSellerId(), trade.getAsset(), trade.getQuantity(), trade.getId());
        trade.setStatus(P2PTradeStatus.CANCELLED);
        restoreAdvertisement(trade);
        return P2PTradeDtos.TradeResponse.from(repository.save(trade));
    }

    @Transactional
    public P2PTradeDtos.TradeResponse dispute(UUID userId, UUID tradeId) {
        P2PTrade trade = get(tradeId);
        if (!trade.getBuyerId().equals(userId) && !trade.getSellerId().equals(userId))
            throw new IllegalArgumentException("Trade does not belong to user");
        if (trade.getStatus() == P2PTradeStatus.DISPUTED) return P2PTradeDtos.TradeResponse.from(trade);
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

    @Scheduled(fixedDelayString = "${oakpay.p2p.expiry-check-ms:60000}")
    @Transactional
    public void expirePendingTrades() {
        LocalDateTime now = LocalDateTime.now();
        repository.findAllByStatusAndExpiresAtBefore(P2PTradeStatus.PAYMENT_PENDING, now).stream()
                .map(P2PTrade::getId)
                .forEach(this::expirePendingTrade);
    }

    private void expirePendingTrade(UUID tradeId) {
        P2PTrade trade = repository.findByIdForUpdate(tradeId).orElse(null);
        if (trade == null || trade.getStatus() != P2PTradeStatus.PAYMENT_PENDING
                || !trade.getExpiresAt().isBefore(LocalDateTime.now())) return;
        walletClient.unlock(trade.getSellerId(), trade.getAsset(), trade.getQuantity(), trade.getId());
        trade.setStatus(P2PTradeStatus.EXPIRED);
        restoreAdvertisement(trade);
        repository.save(trade);
    }

    private boolean containsPaymentMethod(String methods, String selected) {
        return java.util.Arrays.stream(methods.split(","))
                .map(String::trim)
                .map(value -> value.toUpperCase(Locale.ROOT))
                .anyMatch(selected::equals);
    }

    private void validateP2PAsset(String symbol, BigDecimal quantity) {
        SupportedAsset asset = assetRepository.findBySymbolIgnoreCase(symbol)
                .orElseThrow(() -> new IllegalArgumentException("Unsupported asset " + symbol));
        if (asset.getStatus() != AssetStatus.ACTIVE || !asset.getP2pEnabled())
            throw new IllegalStateException("Asset is not enabled for P2P");
        if (quantity.compareTo(asset.getMinTradeAmount()) < 0)
            throw new IllegalArgumentException("Quantity is below the minimum trade amount for " + symbol);
    }

    private P2PTrade get(UUID id) {
        return repository.findById(id).orElseThrow(() -> new IllegalArgumentException("P2P trade not found"));
    }

    private void ensureNotExpired(P2PTrade trade) {
        if (trade.getExpiresAt().isBefore(LocalDateTime.now())) {
            if (trade.getStatus() == P2PTradeStatus.PAYMENT_PENDING) {
                walletClient.unlock(trade.getSellerId(), trade.getAsset(), trade.getQuantity(), trade.getId());
                trade.setStatus(P2PTradeStatus.EXPIRED);
                restoreAdvertisement(trade);
                repository.save(trade);
            }
            throw new IllegalStateException("P2P trade has expired");
        }
    }

    private void restoreAdvertisement(P2PTrade trade) {
        if (trade.getAdvertisementId() == null) return;
        Advertisement ad = advertisementRepository.findByIdForUpdate(trade.getAdvertisementId()).orElse(null);
        if (ad == null) return;
        BigDecimal restored = ad.getAvailableQuantity().add(trade.getQuantity());
        ad.setAvailableQuantity(restored.min(ad.getTotalQuantity()));
        if (ad.getStatus() == AdStatus.CLOSED && ad.getAvailableQuantity().signum() > 0) ad.setStatus(AdStatus.ACTIVE);
        advertisementRepository.save(ad);
    }

    private BigDecimal positive(BigDecimal value, String label) {
        if (value == null || value.signum() <= 0) throw new IllegalArgumentException(label + " must be greater than zero");
        return value.setScale(18, RoundingMode.DOWN);
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Currency is required");
        return value.trim().toUpperCase(Locale.ROOT);
    }
}
