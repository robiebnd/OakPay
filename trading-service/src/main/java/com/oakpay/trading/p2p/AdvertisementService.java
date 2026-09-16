package com.oakpay.trading.p2p;

import com.oakpay.trading.asset.AssetStatus;
import com.oakpay.trading.asset.SupportedAsset;
import com.oakpay.trading.asset.SupportedAssetRepository;
import com.oakpay.trading.security.UserStatusClient;
import com.oakpay.trading.wallet.WalletClient;
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
    private final WalletClient walletClient;
    private final UserStatusClient userStatusClient;

    public AdvertisementService(AdvertisementRepository repository, P2PTradeService tradeService,
                                 SupportedAssetRepository assetRepository, WalletClient walletClient,
                                 UserStatusClient userStatusClient) {
        this.repository = repository; this.tradeService = tradeService;
        this.assetRepository = assetRepository; this.walletClient = walletClient;
        this.userStatusClient = userStatusClient;
    }

    @Transactional
    public AdvertisementDtos.AdResponse create(UUID ownerId, AdvertisementDtos.CreateRequest r) {
        if (!userStatusClient.isActive(ownerId)) throw new IllegalStateException("User account is inactive");
        if (r == null) throw new IllegalArgumentException("Advertisement request is required");
        if (r.side() == null) throw new IllegalArgumentException("Advertisement side is required");
        String asset = normalize(r.asset()), fiat = normalize(r.fiatCurrency());
        if (asset.equals(fiat)) throw new IllegalArgumentException("Asset and fiat currency must differ");
        BigDecimal price = positive(r.price(), "Price"), total = positive(r.totalQuantity(), "Total quantity");
        BigDecimal min = positive(r.minQuantity(), "Minimum quantity"), max = positive(r.maxQuantity(), "Maximum quantity");
        validateP2PAsset(asset, min);
        if (min.compareTo(max) > 0 || max.compareTo(total) > 0) throw new IllegalArgumentException("Quantity limits are invalid");
        if (r.paymentMethods() == null || r.paymentMethods().isBlank()) throw new IllegalArgumentException("At least one payment method is required");
        Advertisement ad = new Advertisement(); ad.setOwnerId(ownerId); ad.setSide(r.side()); ad.setAsset(asset); ad.setFiatCurrency(fiat);
        ad.setPrice(price); ad.setTotalQuantity(total); ad.setAvailableQuantity(total); ad.setMinQuantity(min); ad.setMaxQuantity(max);
        ad.setPaymentMethods(normalizePaymentMethods(r.paymentMethods())); ad.setTerms(r.terms());
        if (r.side() == OrderSide.SELL) {
            String reservationReference = "AD-" + UUID.randomUUID(); ad.setReservationReference(reservationReference);
            walletClient.reserveAdvertisement(ownerId, asset, total, reservationReference);
            try { Advertisement saved = repository.save(ad); return toResponse(saved); }
            catch (RuntimeException ex) { try { walletClient.releaseAdvertisementReservation(ownerId, asset, reservationReference); } catch (RuntimeException ignored) {} throw ex; }
        }
        return toResponse(repository.save(ad));
    }

    @Transactional(readOnly = true)
    public List<AdvertisementDtos.AdResponse> search(OrderSide side, String asset, String fiat, int limit) {
        if (side == null) throw new IllegalArgumentException("Side is required");
        int safe = Math.min(Math.max(limit, 1), 100); String a = normalize(asset), f = normalize(fiat);
        var ads = side == OrderSide.BUY
                ? repository.findAllBySideAndAssetAndFiatCurrencyAndStatusOrderByPriceDescCreatedAtAsc(side, a, f, AdStatus.ACTIVE, PageRequest.of(0, safe))
                : repository.findAllBySideAndAssetAndFiatCurrencyAndStatusOrderByPriceAscCreatedAtAsc(side, a, f, AdStatus.ACTIVE, PageRequest.of(0, safe));
        return ads.stream()
                .filter(x -> x.getAvailableQuantity().signum() > 0)
                .filter(x -> x.getAvailableQuantity().compareTo(x.getMinQuantity()) >= 0)
                .filter(x -> userStatusClient.isActive(x.getOwnerId()))
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AdvertisementDtos.AdResponse get(UUID id) {
        Advertisement ad = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Advertisement not found"));
        if (!userStatusClient.isActive(ad.getOwnerId())) throw new IllegalArgumentException("Advertisement not found");
        return toResponse(ad);
    }

    @Transactional(readOnly = true)
    public List<AdvertisementDtos.AdResponse> mine(UUID ownerId) {
        if (!userStatusClient.isActive(ownerId)) return List.of();
        return repository.findAllByOwnerIdOrderByCreatedAtDesc(ownerId).stream().map(this::toResponse).toList();
    }

    @Transactional
    public AdvertisementDtos.AdResponse update(UUID ownerId, UUID id, AdvertisementDtos.UpdateRequest r) {
        if (!userStatusClient.isActive(ownerId)) throw new IllegalStateException("User account is inactive");
        if (r == null) throw new IllegalArgumentException("Update request is required");
        Advertisement ad = ownedForUpdate(ownerId, id);
        if (ad.getStatus() == AdStatus.CLOSED) throw new IllegalStateException("Advertisement is closed");
        if (r.price() != null) ad.setPrice(positive(r.price(), "Price"));
        if (r.minQuantity() != null) ad.setMinQuantity(positive(r.minQuantity(), "Minimum quantity"));
        if (r.maxQuantity() != null) ad.setMaxQuantity(positive(r.maxQuantity(), "Maximum quantity"));
        validateP2PAsset(ad.getAsset(), ad.getMinQuantity());
        if (ad.getMinQuantity().compareTo(ad.getMaxQuantity()) > 0 || ad.getMaxQuantity().compareTo(ad.getTotalQuantity()) > 0) throw new IllegalArgumentException("Quantity limits are invalid");
        if (r.paymentMethods() != null && !r.paymentMethods().isBlank()) ad.setPaymentMethods(normalizePaymentMethods(r.paymentMethods()));
        if (r.terms() != null) ad.setTerms(r.terms());
        return toResponse(repository.save(ad));
    }

    @Transactional
    public AdvertisementDtos.AdResponse pause(UUID ownerId, UUID id) {
        Advertisement ad = ownedForUpdate(ownerId, id);
        if (ad.getStatus() == AdStatus.CLOSED) throw new IllegalStateException("Advertisement is closed");
        if (ad.getStatus() == AdStatus.PAUSED) return toResponse(ad);
        ad.setStatus(AdStatus.PAUSED);
        return toResponse(repository.save(ad));
    }

    @Transactional
    public AdvertisementDtos.AdResponse resume(UUID ownerId, UUID id) {
        if (!userStatusClient.isActive(ownerId)) throw new IllegalStateException("User account is inactive");
        Advertisement ad = ownedForUpdate(ownerId, id);
        if (ad.getStatus() == AdStatus.CLOSED) throw new IllegalStateException("Advertisement is closed");
        if (ad.getAvailableQuantity().signum() <= 0) throw new IllegalStateException("Advertisement has no available quantity");
        if (ad.getStatus() == AdStatus.ACTIVE) return toResponse(ad);
        ad.setStatus(AdStatus.ACTIVE);
        return toResponse(repository.save(ad));
    }

    @Transactional
    public AdvertisementDtos.AdResponse close(UUID ownerId, UUID id) {
        Advertisement ad = ownedForUpdate(ownerId, id);
        if (ad.getStatus() == AdStatus.CLOSED) return toResponse(ad);
        if (ad.getSide() == OrderSide.SELL && ad.getReservationReference() != null) {
            walletClient.releaseAdvertisementReservation(ownerId, ad.getAsset(), ad.getReservationReference());
        }
        ad.setStatus(AdStatus.CLOSED); ad.setAutoClosed(false);
        return toResponse(repository.save(ad));
    }

    @Transactional
    public P2PTradeDtos.TradeResponse take(UUID takerId, UUID adId, AdvertisementDtos.TakeRequest r) {
        if (!userStatusClient.isActive(takerId)) throw new IllegalStateException("User account is inactive");
        if (r == null) throw new IllegalArgumentException("Take request is required");
        Advertisement ad = ownedForUpdate(null, adId);
        if (!userStatusClient.isActive(ad.getOwnerId())) throw new IllegalStateException("Advertisement owner is inactive");
        if (ad.getOwnerId().equals(takerId)) throw new IllegalArgumentException("You cannot take your own advertisement");
        if (ad.getStatus() != AdStatus.ACTIVE) throw new IllegalStateException("Advertisement is not active");
        BigDecimal quantity = positive(r.quantity(), "Quantity"); validateP2PAsset(ad.getAsset(), quantity);
        if (quantity.compareTo(ad.getMinQuantity()) < 0 || quantity.compareTo(ad.getMaxQuantity()) > 0 || quantity.compareTo(ad.getAvailableQuantity()) > 0)
            throw new IllegalArgumentException("Quantity is outside the advertisement limits");
        String paymentMethod = normalizePaymentMethod(r.paymentMethod());
        if (!containsPaymentMethod(ad.getPaymentMethods(), paymentMethod)) throw new IllegalArgumentException("Selected payment method is not supported by this advertisement");
        return tradeService.create(takerId, new P2PTradeDtos.CreateRequest(
                ad.getSide() == OrderSide.BUY ? ad.getOwnerId() : null,
                ad.getAsset(), ad.getFiatCurrency(), quantity, ad.getPrice(), paymentMethod, r.expiryMinutes(), ad.getId()));
    }

    private AdvertisementDtos.AdResponse toResponse(Advertisement ad) {
        return AdvertisementDtos.AdResponse.from(ad, userStatusClient.displayName(ad.getOwnerId()));
    }

    private void validateP2PAsset(String symbol, BigDecimal quantity) {
        SupportedAsset asset = assetRepository.findBySymbolIgnoreCase(symbol).orElseThrow(() -> new IllegalArgumentException("Unsupported asset " + symbol));
        if (asset.getStatus() != AssetStatus.ACTIVE || !asset.getP2pEnabled()) throw new IllegalStateException("Asset is not enabled for P2P");
        if (quantity.compareTo(asset.getMinTradeAmount()) < 0) throw new IllegalArgumentException("Quantity is below the minimum trade amount for " + symbol);
    }

    private boolean containsPaymentMethod(String methods, String selected) {
        return java.util.Arrays.stream(methods.split(",")).map(String::trim).map(v -> v.toUpperCase(Locale.ROOT)).anyMatch(selected::equals);
    }

    private String normalizePaymentMethod(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Payment method is required");
        return value.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizePaymentMethods(String value) {
        return java.util.Arrays.stream(value.split(","))
                .map(String::trim).filter(v -> !v.isBlank()).map(v -> v.toUpperCase(Locale.ROOT)).distinct().reduce((a,b) -> a + "," + b)
                .orElseThrow(() -> new IllegalArgumentException("At least one payment method is required"));
    }

    private Advertisement ownedForUpdate(UUID ownerId, UUID id) {
        Advertisement ad = repository.findByIdForUpdate(id).orElseThrow(() -> new IllegalArgumentException("Advertisement not found"));
        if (ownerId != null && !ad.getOwnerId().equals(ownerId)) throw new IllegalArgumentException("Advertisement does not belong to user");
        if (!userStatusClient.isActive(ad.getOwnerId())) throw new IllegalStateException("Advertisement owner is inactive");
        return ad;
    }

    private BigDecimal positive(BigDecimal value, String label) {
        if (value == null || value.signum() <= 0) throw new IllegalArgumentException(label + " must be greater than zero");
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Currency is required");
        return value.trim().toUpperCase(Locale.ROOT);
    }
}
