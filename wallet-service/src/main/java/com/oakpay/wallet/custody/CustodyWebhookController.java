package com.oakpay.wallet.custody;

import com.oakpay.wallet.deposit.DepositDtos;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/v1/webhooks/custody")
public class CustodyWebhookController {
    private final CustodyWebhookVerifier verifier;
    private final CustodyWebhookEventService eventService;

    public CustodyWebhookController(CustodyWebhookVerifier verifier,
                                    CustodyWebhookEventService eventService) {
        this.verifier = verifier;
        this.eventService = eventService;
    }

    @PostMapping("/{provider}/deposits")
    public DepositDtos.DepositResponse depositWebhook(
            @PathVariable String provider,
            @RequestHeader("X-OakPay-Webhook-Id") String eventId,
            @RequestHeader("X-OakPay-Webhook-Timestamp") String timestamp,
            @RequestHeader("X-OakPay-Webhook-Signature") String signature,
            HttpServletRequest request) {
        try {
            String rawBody = new String(request.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            verifier.verify(provider, eventId, timestamp, signature, rawBody);
            return eventService.processDeposit(provider, eventId, rawBody);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unable to read webhook payload", e);
        }
    }
    @PostMapping("/{provider}/withdrawals")
    public CustodyWithdrawalDtos.WithdrawalResponse withdrawalWebhook(
            @PathVariable String provider,
            @RequestHeader("X-OakPay-Webhook-Id") String eventId,
            @RequestHeader("X-OakPay-Webhook-Timestamp") String timestamp,
            @RequestHeader("X-OakPay-Webhook-Signature") String signature,
            HttpServletRequest request) {
        try {
            String rawBody = new String(request.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            verifier.verify(provider, eventId, timestamp, signature, rawBody);
            return eventService.processWithdrawal(provider, eventId, rawBody);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unable to read webhook payload", e);
        }
    }

}
