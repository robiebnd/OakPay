package com.oakpay.wallet.internal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/wallets")
public class InternalWalletController {
    private final InternalWalletService service; private final String secret;
    public InternalWalletController(InternalWalletService service, @Value("${oakpay.internal-secret}") String secret) { this.service = service; this.secret = secret; }
    @GetMapping("/internal/{userId}/{currency}/balance")
    public InternalWalletDtos.BalanceResponse balance(@PathVariable UUID userId, @PathVariable String currency, @RequestHeader(value="X-OakPay-Internal-Secret", required=false) String supplied) { authorize(supplied); return service.balance(userId, currency); }
    @PostMapping("/{currency}/lock") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void lock(@PathVariable String currency, @RequestHeader(value="X-OakPay-Internal-Secret", required=false) String supplied, @RequestBody InternalWalletDtos.MutationRequest request) { authorize(supplied); service.lock(request.userId(), currency, request); }
    @PostMapping("/{currency}/unlock") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unlock(@PathVariable String currency, @RequestHeader(value="X-OakPay-Internal-Secret", required=false) String supplied, @RequestBody InternalWalletDtos.MutationRequest request) { authorize(supplied); service.unlock(request.userId(), currency, request); }
    @PostMapping("/internal/advertisement-reservations/{userId}/{currency}/reserve") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reserveAdvertisement(@PathVariable UUID userId, @PathVariable String currency, @RequestHeader(value="X-OakPay-Internal-Secret", required=false) String supplied, @RequestBody InternalWalletDtos.ReservationRequest request) { authorize(supplied); service.reserveAdvertisement(userId, currency, request); }
    @PostMapping("/internal/advertisement-reservations/{userId}/{currency}/consume") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void consumeAdvertisement(@PathVariable UUID userId, @PathVariable String currency, @RequestHeader(value="X-OakPay-Internal-Secret", required=false) String supplied, @RequestBody InternalWalletDtos.ReservationRequest request) { authorize(supplied); service.consumeAdvertisementReservation(userId, currency, request); }
    @PostMapping("/internal/advertisement-reservations/{userId}/{currency}/release") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void releaseAdvertisement(@PathVariable UUID userId, @PathVariable String currency, @RequestHeader(value="X-OakPay-Internal-Secret", required=false) String supplied, @RequestBody InternalWalletDtos.ReservationRequest request) { authorize(supplied); service.releaseAdvertisementReservation(userId, currency, request); }
    @PostMapping("/internal/settle") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void settle(@RequestHeader(value="X-OakPay-Internal-Secret", required=false) String supplied, @RequestBody InternalWalletDtos.SettlementRequest request) { authorize(supplied); service.settle(request); }
    private void authorize(String supplied) { if (secret == null || secret.isBlank() || !secret.equals(supplied)) throw new org.springframework.web.server.ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid internal credential"); }
}
