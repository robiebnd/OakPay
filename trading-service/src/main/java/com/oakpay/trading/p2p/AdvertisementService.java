package com.oakpay.trading.p2p;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Service
public class AdvertisementService {
    private final AdvertisementRepository repository;
    private final P2PTradeService tradeService;

    public AdvertisementService(AdvertisementRepository repository, P2PTradeService tradeService) {
        this.repository = repository;
        this.tradeService = tradeService;
    }

    @Transactional
    public AdvertisementDtos.AdResponse create(UUID ownerId, AdvertisementDtos.CreateRequest r) {
        if (r.side() == null) throw new IllegalArgumentException("Advertisement side is required");
        String asset = normalize(r.asset()), fiat = normalize(r.fiatCurrency());
        if (asset.equals(fiat)) throw new IllegalArgumentException("Asset and fiat currency must differ");
        BigDecimal price = positive(r.price(), "Price");
        BigDecimal total = positive(r.totalQuantity(), "Total quantity");
        BigDecimal min = positive(r.minQuantity(), "Minimum quantity");
        BigDecimal max = positive(r.maxQuantity(), "Maximum quantity");
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
        return repository.findAllBySideAndAssetAndFiatCurrencyAndStatusOrderByPriceAscCreatedAtAsc(
                        side, normalize(asset), normalize(fiat), AdStatus.ACTIVE, PageRequest.of(0, safeLimit))
                .stream().filter(a -> a.getAvailableQuantity().signum() > 0)
                .map(AdvertisementDtos.AdResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<AdvertisementDtos.AdResponse> mine(UUID ownerId) {
        return repository.findAllByOwnerIdOrderByCreatedAtDesc(ownerId).stream()
                .map(AdvertisementDtos.AdResponse::from).toList();
    }

    @Transactional
    public AdvertisementDtos.AdResponse update(UUID ownerId, UUID id, AdvertisementDtos.UpdateRequest r) {
        Advertisement ad = ownedForUpdate(ownerId, id);
        if (ad.getStatus() == AdStatus.CLOSED) throw new IllegalStateException("Advertisement is closed");
        if (r.price() != null) ad.setPrice(positive(r.price(), "Price"));
        if (r.minQuantity() != null) ad.setMinQuantity(positive(r.minQuantity(), "Minimum quantity"));
        if (r.maxQuantity() != null) ad.setMaxQuantity(positive(r.maxQuantity(), "Maximum quantity"));
        if (ad.getMinQuantity().compareTo(ad.getMaxQuantity()) > 0)
            throw new IllegalArgumentException("Minimum quantity cannot exceed maximum quantity");
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
        return AdvertisementDtos.AdResponse.from(repository.save(ad));
    }

    @Transactional
    public P2PTradeDtos.TradeResponse take(UUID buyerOrSellerId, UUID adId, AdvertisementDtos.TakeRequest r) {
        Advertisement ad = ownedForUpdate(null, adId);
        if (ad.getOwnerId().equals(buyerOrSellerId)) throw new IllegalArgumentException("You cannot take your own advertisement");
        if (ad.getStatus() != AdStatus.ACTIVE) throw new IllegalStateException("Advertisement is not active");
        BigDecimal quantity = positive(r.quantity(), "Quantity");
        if (quantity.compareTo(ad.getMinQuantity()) < 0 || quantity.compareTo(ad.getMaxQuantity()) > 0)
            throw new IllegalArgumentException("Quantity is outside the advertisement limits");
        if (quantity.compareTo(ad.getAvailableQuantity()) > 0)
            throw new IllegalStateException("Advertisement has insufficient available quantity");

        UUID sellerId = ad.getSide() == OrderSide.SELL ? ad.getOwnerId() : buyerOrSellerId;
        UUID buyerId = ad.getSide() == OrderSide.SELL ? buyerOrSellerId : ad.getOwnerId();
        P2PTradeDtos.CreateRequest request = new P2PTradeDtos.CreateRequest(
                buyerId, ad.getAsset(), ad.getFiatCurrency(), quantity, ad.getPrice(), firstPaymentMethod(ad.getPaymentMethods()),
                r.expiryMinutes());
        P2PTradeDtos.TradeResponse trade = tradeService.create(sellerId, request);

        ad.setAvailableQuantity(ad.getAvailableQuantity().subtract(quantity));
        if (ad.getAvailableQuantity().signum() == 0) ad.setStatus(AdStatus.CLOSED);
        repository.save(ad);
        return trade;
    }

    private Advertisement ownedForUpdate(UUID ownerId, UUID id) {
        Advertisement ad = repository.findByIdForUpdate(id)
                .orElseThrow(() -> new IllegalArgumentException("Advertisement not found"));
        if (ownerId != null && !ad.getOwnerId().equals(ownerId))
            throw new IllegalArgumentException("Advertisement does not belong to user");
        return ad;
    }

    private String firstPaymentMethod(String methods) {
        String[] values = methods.split(",");
        return values[0].trim();
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
