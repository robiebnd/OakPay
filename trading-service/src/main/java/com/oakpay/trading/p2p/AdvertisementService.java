package com.oakpay.trading.p2p;

import com.oakpay.trading.asset.AssetStatus;
import com.oakpay.trading.asset.SupportedAsset;
import com.oakpay.trading.asset.SupportedAssetRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class AdvertisementService {
    private final AdvertisementRepository repository;
    private final P2PTradeService tradeService;
    private final SupportedAssetRepository assetRepository;

    public AdvertisementService(AdvertisementRepository repository, P2PTradeService tradeService,
                                 SupportedAssetRepository assetRepository) {
        this.repository = repository;
        this.tradeService = tradeService;
        this.assetRepository = assetRepository;
    }

    @Transactional
    public AdvertisementDtos.AdResponse create(UUID ownerId, AdvertisementDtos.CreateRequest r) {
        if (r == null) throw new IllegalArgumentException("Advertisement request is required");
        if (r.side() == null) throw new IllegalArgumentException("Advertisement side is required");
        String asset = normalize(r.asset()), fiat = normalize(r.fiatCurrency());
        if (asset.equals(fiat)) throw new IllegalArgumentException("Asset and fiat currency must differ");
        BigDecimal price = positive(r.price(), "Price");
        BigDecimal total = positive(r.totalQuantity(), "Total quantity");
        BigDecimal min = positive(r.minQuantity(), "Minimum quantity");
        BigDecimal max = positive(r.maxQuantity(), "Maximum quantity");
        validateP2PAsset(asset, min);
        if (min.compareTo(max) > 0 || max.compareTo(total) > 0)
            throw new IllegalArgumentException("Quantity limits are invalid");
        if (r.paymentMethods() == null || r.paymentMethods().isBlank())
            throw new IllegalArgumentException("At least one payment method is required");

        Advertisement ad = new Advertisement();
        ad.setOwnerId(ownerId);
        ad.setSide(r.side());
        ad.setAsset(asset);
        ad.setFiatCurrency(fiat);
        ad.setPrice(price);
        ad.setTotalQuantity(total);
        ad.setAvailableQuantity(total);
        ad.setMinQuantity(min);
        ad.setMaxQuantity(max);
        ad.setPaymentMethods(r.paymentMethods().trim());
        ad.setTerms(r.terms());
        return AdvertisementDtos.AdResponse.from(repository.save(ad));
    }

    @Transactional(readOnly = true)
    public List<AdvertisementDtos.AdResponse> search(OrderSide side, String asset, String fiat, int limit) {
        if (side == null) throw new IllegalArgumentException("Side is required");
        int safeLimit = Math.min(Math.max(limit, 1), 100);
        String normalizedAsset = normalize(asset), normalizedFiat = normalize(fiat);
        var ads = side == OrderSide.BUY
                ? repository.findAllBySideAndAssetAndFiatCurrencyAndStatusOrderByPriceDescCreatedAtAsc(
                    side, normalizedAsset, normalizedFiat, AdStatus.ACTIVE, PageRequest.of(0, safeLimit))
                : repository.findAllBySideAndAssetAndFiatCurrencyAndStatusOrderByPriceAscCreatedAtAsc(
                    side, normalizedAsset, normalizedFiat, AdStatus.ACTIVE, PageRequest.of(0, safeLimit));
        return ads.stream().filter(a -> a.getAvailableQuantity().signum() > 0)
                .map(AdvertisementDtos.AdResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<AdvertisementDtos.AdResponse> mine(UUID ownerId) {
        return repository.findAllByOwnerIdOrderByCreatedAtDesc(ownerId).stream()
                .map(AdvertisementDtos.AdResponse::from).toList();
    }

    @Transactional
    public AdvertisementDtos.AdResponse update(UUID ownerId, UUID id, AdvertisementDtos.UpdateRequest r) {
        if (r == null) throw new IllegalArgumentException("Update request is required");
        Advertisement ad = ownedForUpdate(ownerId, id);
        if (ad.getStatus() == AdStatus.CLOSED) throw new IllegalStateException("Advertisement is closed");
        if (r.price() != null) ad.setPrice(positive(r.price(), "Price"));
        if (r.minQuantity() != null) ad.setMinQuantity(positive(r.minQuantity(), "Minimum quantity"));
        if (r.maxQuantity() != null) ad.setMaxQuantity(positive(r.maxQuantity(), "Maximum quantity"));
        validateP2PAsset(ad.getAsset(), ad.getMinQuantity());
        if (ad.getMinQuantity().compareTo(ad.getMaxQuantity()) > 0 || ad.getMaxQuantity().compareTo(ad.getTotalQuantity()) > 0)
            throw new IllegalArgumentException("Quantity limits are invalid");
        if (r.paymentMethods() != null && !r.paymentMethods().isBlank()) ad.setPaymentMethods(r.paymentMethods().trim());
        if (r.terms() != null) ad.setTerms(r.terms());
        return AdvertisementDtos.AdResponse.from(repository.save(ad));
    }

    @Transactional
    public AdvertisementDtos.AdResponse pause(UUID ownerId, UUID id) {
        Advertisement ad = ownedForUpdate(ownerId, id);
        if (ad.getStatus() == AdStatus.CLOSED) throw new IllegalStateException("Advertisement is closed");
        ad.setStatus(ad.getStatus() == AdStatus.PAUSED ? AdStatus.ACTIVE : AdStatus.PAUSED);
        return AdvertisementDtos.AdResponse.from(repository.save(ad));
    }

    @Transactional
    public AdvertisementDtos.AdResponse close(UUID ownerId, UUID id) {
        Advertisement ad = ownedForUpdate(ownerId, id);
        ad.setStatus(AdStatus.CLOSED);
        ad.setAutoClosed(false);
        return AdvertisementDtos.AdResponse.from(repository.save(ad));
    }

    @Transactional
    public P2PTradeDtos.TradeResponse take(UUID takerId, UUID adId, AdvertisementDtos.TakeRequest r) {
        if (r == null) throw new IllegalArgumentException("Take request is required");
        Advertisement ad = ownedForUpdate(null, adId);
        if (ad.getOwnerId().equals(takerId)) throw new IllegalArgumentException("You cannot take your own advertisement");
        if (ad.getStatus() != AdStatus.ACTIVE) throw new IllegalStateException("Advertisement is not active");
        BigDecimal quantity = positive(r.quantity(), "Quantity");
        validateP2PAsset(ad.getAsset(), quantity);
        if (quantity.compareTo(ad.getMinQuantity()) < 0 || quantity.compareTo(ad.getMaxQuantity()) > 0)
            throw new IllegalArgumentException("Quantity is outside the advertisement limits");
        if (quantity.compareTo(ad.getAvailableQuantity()) > 0)
            throw new IllegalStateException("Advertisement has insufficient available quantity");

        String paymentMethod = normalizePaymentMethod(r.paymentMethod());
        if (!containsPaymentMethod(ad.getPaymentMethods(), paymentMethod))
            throw new IllegalArgumentException("Selected payment method is not supported by this advertisement");

        UUID sellerId = ad.getSide() == OrderSide.SELL ? ad.getOwnerId() : takerId;
        UUID buyerId = ad.getSide() == OrderSide.SELL ? takerId : ad.getOwnerId();
        P2PTradeDtos.CreateRequest request = new P2PTradeDtos.CreateRequest(
                buyerId, ad.getAsset(), ad.getFiatCurrency(), quantity, ad.getPrice(),
                paymentMethod, r.expiryMinutes(), ad.getId());
        return tradeService.create(sellerId, request);
    }

    private void validateP2PAsset(String symbol, BigDecimal quantity) {
        SupportedAsset asset = assetRepository.findBySymbolIgnoreCase(symbol)
                .orElseThrow(() -> new IllegalArgumentException("Unsupported asset " + symbol));
        if (asset.getStatus() != AssetStatus.ACTIVE || !asset.getP2pEnabled())
            throw new IllegalStateException("Asset is not enabled for P2P");
        if (quantity.compareTo(asset.getMinTradeAmount()) < 0)
            throw new IllegalArgumentException("Quantity is below the minimum trade amount for " + symbol);
    }

    private boolean containsPaymentMethod(String methods, String selected) {
        return java.util.Arrays.stream(methods.split(","))
                .map(String::trim)
                .map(value -> value.toUpperCase(Locale.ROOT))
                .anyMatch(selected::equals);
    }

    private String normalizePaymentMethod(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Payment method is required");
        return value.trim().toUpperCase(Locale.ROOT);
    }

    private Advertisement ownedForUpdate(UUID ownerId, UUID id) {
        Advertisement ad = repository.findByIdForUpdate(id)
                .orElseThrow(() -> new IllegalArgumentException("Advertisement not found"));
        if (ownerId != null && !ad.getOwnerId().equals(ownerId))
            throw new IllegalArgumentException("Advertisement does not belong to user");
        return ad;
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
