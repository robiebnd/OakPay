package com.oakpay.trading.p2p;

import com.oakpay.trading.asset.SupportedAssetRepository;
import com.oakpay.trading.wallet.WalletClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class P2PTradeServiceTest {

    @Mock P2PTradeRepository repository;
    @Mock WalletClient walletClient;
    @Mock P2PPaymentService paymentService;
    @Mock P2PPaymentRepository paymentRepository;
    @Mock P2PCommissionService commissionService;
    @Mock SupportedAssetRepository assetRepository;
    @Mock AdvertisementRepository advertisementRepository;

    private P2PTradeService service;

    @BeforeEach
    void setUp() {
        service = new P2PTradeService(
                repository,
                walletClient,
                paymentService,
                paymentRepository,
                commissionService,
                assetRepository,
                advertisementRepository
        );
    }

    @Test
    void expirePendingTradesUnlocksFundsAndRestoresAdvertisement() {
        UUID tradeId = UUID.randomUUID();
        UUID sellerId = UUID.randomUUID();
        UUID buyerId = UUID.randomUUID();
        UUID adId = UUID.randomUUID();

        P2PTrade trade = pendingTrade(tradeId, sellerId, buyerId, adId, 10,
                LocalDateTime.now().minusMinutes(1));
        Advertisement ad = advertisement(adId, 70);

        when(repository.findAllByStatusOrderByCreatedAtAsc(P2PTradeStatus.PAYMENT_PENDING))
                .thenReturn(List.of(trade));
        when(repository.findByIdForUpdate(tradeId)).thenReturn(Optional.of(trade));
        when(advertisementRepository.findByIdForUpdate(adId)).thenReturn(Optional.of(ad));
        when(repository.save(trade)).thenReturn(trade);
        when(advertisementRepository.save(ad)).thenReturn(ad);

        service.expirePendingTrades();

        assertEquals(P2PTradeStatus.EXPIRED, trade.getStatus());
        assertEquals(new BigDecimal("80"), ad.getAvailableQuantity());
        verify(walletClient).unlock(eq(sellerId), eq("USDT"), eq(new BigDecimal("10")), eq(tradeId));
        verify(repository).save(trade);
        verify(advertisementRepository).save(ad);
    }

    @Test
    void expirePendingTradesDoesNotExpireFutureTrade() {
        UUID tradeId = UUID.randomUUID();
        UUID sellerId = UUID.randomUUID();
        UUID buyerId = UUID.randomUUID();
        UUID adId = UUID.randomUUID();

        P2PTrade trade = pendingTrade(tradeId, sellerId, buyerId, adId, 10,
                LocalDateTime.now().plusMinutes(5));

        when(repository.findAllByStatusOrderByCreatedAtAsc(P2PTradeStatus.PAYMENT_PENDING))
                .thenReturn(List.of(trade));

        service.expirePendingTrades();

        assertEquals(P2PTradeStatus.PAYMENT_PENDING, trade.getStatus());
        verify(repository, never()).findByIdForUpdate(tradeId);
        verify(walletClient, never()).unlock(any(), anyString(), any(), any());
    }

    @Test
    void expirePendingTradesDoesNotRestoreMoreThanAdvertisementTotal() {
        UUID tradeId = UUID.randomUUID();
        UUID sellerId = UUID.randomUUID();
        UUID buyerId = UUID.randomUUID();
        UUID adId = UUID.randomUUID();

        P2PTrade trade = pendingTrade(tradeId, sellerId, buyerId, adId, 20,
                LocalDateTime.now().minusMinutes(1));
        Advertisement ad = advertisement(adId, 90);
        ad.setTotalQuantity(new BigDecimal("100"));

        when(repository.findAllByStatusOrderByCreatedAtAsc(P2PTradeStatus.PAYMENT_PENDING))
                .thenReturn(List.of(trade));
        when(repository.findByIdForUpdate(tradeId)).thenReturn(Optional.of(trade));
        when(advertisementRepository.findByIdForUpdate(adId)).thenReturn(Optional.of(ad));
        when(repository.save(trade)).thenReturn(trade);
        when(advertisementRepository.save(ad)).thenReturn(ad);

        service.expirePendingTrades();

        assertEquals(P2PTradeStatus.EXPIRED, trade.getStatus());
        assertEquals(new BigDecimal("100"), ad.getAvailableQuantity());
    }

    private P2PTrade pendingTrade(UUID id, UUID sellerId, UUID buyerId, UUID adId,
                                   int quantity, LocalDateTime expiresAt) {
        P2PTrade trade = new P2PTrade();
        trade.setBuyerId(buyerId);
        trade.setSellerId(sellerId);
        trade.setAdvertisementId(adId);
        trade.setAsset("USDT");
        trade.setFiatCurrency("ZWL");
        trade.setQuantity(new BigDecimal(quantity));
        trade.setUnitPrice(new BigDecimal("35000"));
        trade.setFiatAmount(new BigDecimal(quantity).multiply(new BigDecimal("35000")));
        trade.setPaymentMethod("ECOCASH");
        trade.setExpiresAt(expiresAt);
        trade.setStatus(P2PTradeStatus.PAYMENT_PENDING);
        setId(trade, id);
        return trade;
    }

    private Advertisement advertisement(UUID id, int available) {
        Advertisement ad = new Advertisement();
        setAdvertisementId(ad, id);
        ad.setTotalQuantity(new BigDecimal("100"));
        ad.setAvailableQuantity(new BigDecimal(available));
        ad.setStatus(AdStatus.ACTIVE);
        ad.setAutoClosed(false);
        return ad;
    }

    private void setId(P2PTrade trade, UUID id) {
        try {
            var field = P2PTrade.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(trade, id);
        } catch (ReflectiveOperationException ex) {
            throw new AssertionError(ex);
        }
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
