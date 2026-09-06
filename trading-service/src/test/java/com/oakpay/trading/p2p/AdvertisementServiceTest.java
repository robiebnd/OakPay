package com.oakpay.trading.p2p;

import com.oakpay.trading.asset.AssetStatus;
import com.oakpay.trading.asset.SupportedAsset;
import com.oakpay.trading.asset.SupportedAssetRepository;
import com.oakpay.trading.wallet.WalletClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdvertisementServiceTest {

    @Mock AdvertisementRepository repository;
    @Mock P2PTradeService tradeService;
    @Mock SupportedAssetRepository assetRepository;
    @Mock WalletClient walletClient;

    private AdvertisementService service;

    @BeforeEach
    void setUp() {
        service = new AdvertisementService(repository, tradeService, assetRepository, walletClient);
    }

    @Test
    void sellerCannotCreateAdvertisementWhenExistingOpenAdsConsumeBalance() {
        UUID sellerId = UUID.randomUUID();
        SupportedAsset asset = usdtAsset();

        when(assetRepository.findBySymbolIgnoreCase("USDT")).thenReturn(Optional.of(asset));
        when(walletClient.availableBalance(sellerId, "USDT")).thenReturn(new BigDecimal("100"));
        when(repository.sumAvailableQuantityByOwnerAndSideAndAssetAndStatuses(
                eq(sellerId), eq(OrderSide.SELL), eq("USDT"), anyList()))
                .thenReturn(new BigDecimal("80"));

        AdvertisementDtos.CreateRequest request = new AdvertisementDtos.CreateRequest(
                OrderSide.SELL, "USDT", "ZWL", new BigDecimal("35000"),
                new BigDecimal("30"), new BigDecimal("1"), new BigDecimal("30"),
                "ECOCASH", "");

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.create(sellerId, request));

        assertEquals("Insufficient available USDT balance. Existing open advertisements reserve 80 USDT and this advertisement requires 30 USDT", ex.getMessage());
        verify(repository, never()).save(any(Advertisement.class));
    }

    @Test
    void closedAdvertisementCannotBeClosedAgain() {
        UUID sellerId = UUID.randomUUID();
        UUID adId = UUID.randomUUID();
        Advertisement ad = advertisement(adId, sellerId, AdStatus.CLOSED);

        when(repository.findByIdForUpdate(adId)).thenReturn(Optional.of(ad));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.close(sellerId, adId));

        assertEquals("Advertisement is already closed", ex.getMessage());
        verify(repository, never()).save(any(Advertisement.class));
    }

    @Test
    void pauseTogglesBetweenActiveAndPaused() {
        UUID sellerId = UUID.randomUUID();
        UUID adId = UUID.randomUUID();
        Advertisement ad = advertisement(adId, sellerId, AdStatus.ACTIVE);

        when(repository.findByIdForUpdate(adId)).thenReturn(Optional.of(ad));
        when(repository.save(ad)).thenReturn(ad);

        service.pause(sellerId, adId);
        assertEquals(AdStatus.PAUSED, ad.getStatus());

        service.pause(sellerId, adId);
        assertEquals(AdStatus.ACTIVE, ad.getStatus());
        verify(repository, times(2)).save(ad);
    }

    private SupportedAsset usdtAsset() {
        SupportedAsset asset = new SupportedAsset();
        asset.setSymbol("USDT");
        asset.setStatus(AssetStatus.ACTIVE);
        asset.setP2pEnabled(true);
        asset.setMinTradeAmount(new BigDecimal("1"));
        return asset;
    }

    private Advertisement advertisement(UUID id, UUID ownerId, AdStatus status) {
        Advertisement ad = new Advertisement();
        setAdvertisementId(ad, id);
        ad.setOwnerId(ownerId);
        ad.setSide(OrderSide.SELL);
        ad.setAsset("USDT");
        ad.setFiatCurrency("ZWL");
        ad.setPrice(new BigDecimal("35000"));
        ad.setTotalQuantity(new BigDecimal("100"));
        ad.setAvailableQuantity(new BigDecimal("100"));
        ad.setMinQuantity(new BigDecimal("1"));
        ad.setMaxQuantity(new BigDecimal("100"));
        ad.setPaymentMethods("ECOCASH");
        ad.setStatus(status);
        return ad;
    }

    private void setAdvertisementId(Advertisement ad, UUID id) {
        try {
            var field = Advertisement.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(ad, id);
        } catch (ReflectiveOperationException ex) {
            throw new AssertionError(ex);
        }
    }
}
