package com.oakpay.trading.wallet;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import java.math.BigDecimal;
import java.util.UUID;

@Component
public class WalletClient {
    private final RestClient client; private final String internalSecret;
    public WalletClient(@Value("${oakpay.wallet.base-url}") String baseUrl, @Value("${oakpay.internal-secret}") String internalSecret) { this.client = RestClient.builder().baseUrl(baseUrl).build(); this.internalSecret = internalSecret; }
    public BigDecimal availableBalance(UUID userId, String currency) { WalletBalance response = client.get().uri("/api/v1/wallets/internal/{userId}/{currency}/balance", userId, currency).header("X-OakPay-Internal-Secret", internalSecret).retrieve().body(WalletBalance.class); if (response == null || response.availableBalance() == null) throw new IllegalStateException("Wallet balance could not be retrieved"); return response.availableBalance(); }
    public void lock(UUID userId, String currency, BigDecimal amount, UUID referenceId) { client.post().uri("/api/v1/wallets/{currency}/lock", currency).contentType(MediaType.APPLICATION_JSON).header("X-OakPay-Internal-Secret", internalSecret).body(new WalletMutation(userId, amount, referenceId.toString())).retrieve().toBodilessEntity(); }
    public void unlock(UUID userId, String currency, BigDecimal amount, UUID referenceId) { client.post().uri("/api/v1/wallets/{currency}/unlock", currency).contentType(MediaType.APPLICATION_JSON).header("X-OakPay-Internal-Secret", internalSecret).body(new WalletMutation(userId, amount, referenceId.toString())).retrieve().toBodilessEntity(); }
    public void reserveAdvertisement(UUID userId, String currency, BigDecimal amount, String reservationReference) { client.post().uri("/api/v1/wallets/internal/advertisement-reservations/{userId}/{currency}/reserve", userId, currency).contentType(MediaType.APPLICATION_JSON).header("X-OakPay-Internal-Secret", internalSecret).body(new ReservationRequest(amount, reservationReference, null)).retrieve().toBodilessEntity(); }
    public void consumeAdvertisementReservation(UUID userId, String currency, BigDecimal amount, String reservationReference, UUID tradeId) { client.post().uri("/api/v1/wallets/internal/advertisement-reservations/{userId}/{currency}/consume", userId, currency).contentType(MediaType.APPLICATION_JSON).header("X-OakPay-Internal-Secret", internalSecret).body(new ReservationRequest(amount, reservationReference, tradeId.toString())).retrieve().toBodilessEntity(); }
    public void releaseAdvertisementReservation(UUID userId, String currency, String reservationReference) { client.post().uri("/api/v1/wallets/internal/advertisement-reservations/{userId}/{currency}/release", userId, currency).contentType(MediaType.APPLICATION_JSON).header("X-OakPay-Internal-Secret", internalSecret).body(new ReservationRequest(BigDecimal.ONE, reservationReference, null)).retrieve().toBodilessEntity(); }
    public void settle(Settlement settlement) { client.post().uri("/api/v1/wallets/internal/settle").contentType(MediaType.APPLICATION_JSON).header("X-OakPay-Internal-Secret", internalSecret).body(settlement).retrieve().toBodilessEntity(); }
    public void releaseEscrow(UUID sellerId, UUID buyerId, String asset, BigDecimal amount, UUID tradeId) { client.post().uri("/api/v1/wallets/internal/escrow/release").contentType(MediaType.APPLICATION_JSON).header("X-OakPay-Internal-Secret", internalSecret).body(new EscrowRelease(sellerId, buyerId, asset, amount, tradeId.toString())).retrieve().toBodilessEntity(); }
    public record WalletBalance(UUID userId, String currency, BigDecimal availableBalance, BigDecimal lockedBalance) {}
    public record WalletMutation(UUID userId, BigDecimal amount, String reference) {}
    public record ReservationRequest(BigDecimal amount, String reference, String operationReference) {}
    public record Settlement(UUID buyerId, UUID sellerId, String baseCurrency, String quoteCurrency, BigDecimal baseAmount, BigDecimal quoteAmount, BigDecimal buyerFee, BigDecimal sellerFee, String reference) {}
    public record EscrowRelease(UUID sellerId, UUID buyerId, String asset, BigDecimal amount, String reference) {}
}
