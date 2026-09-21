package com.oakpay.wallet.custody;

import com.oakpay.wallet.deposit.DepositDtos;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/v1/webhooks/bitgo")
@ConditionalOnBean(BitGoWebhookService.class)
public class BitGoWebhookController {
    private final BitGoWebhookService webhookService;

    public BitGoWebhookController(BitGoWebhookService webhookService) {
        this.webhookService = webhookService;
    }

    @PostMapping("/{coin}/transfers")
    public DepositDtos.DepositResponse transferWebhook(@PathVariable String coin,
            @RequestHeader("X-Signature-SHA256") String signature, HttpServletRequest request) {
        try {
            String rawBody = new String(request.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            return webhookService.processTransferNotification(coin, signature, rawBody);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unable to read BitGo webhook payload", e);
        }
    }
}
