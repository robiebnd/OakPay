package com.oakpay.wallet.custody;

import com.oakpay.wallet.deposit.DepositAddressDtos;
import com.oakpay.wallet.deposit.DepositAddressService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/wallets/custody/tatum")
@ConditionalOnProperty(name = "oakpay.custody.provider-name", havingValue = "TATUM", matchIfMissing = true)
public class TatumDepositAddressController {
    private final CustodyProviderService custodyProviderService;
    private final DepositAddressService depositAddressService;
    private final String internalSecret;

    public TatumDepositAddressController(CustodyProviderService custodyProviderService,
                                         DepositAddressService depositAddressService,
                                         @Value("${oakpay.internal-secret}") String internalSecret) {
        this.custodyProviderService = custodyProviderService;
        this.depositAddressService = depositAddressService;
        this.internalSecret = internalSecret;
    }

    @PostMapping("/deposit-addresses")
    public ResponseEntity<?> create(
            @RequestHeader(value = "X-OakPay-Internal-Secret", required = false) String suppliedSecret,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody CreateDepositAddressRequest request) {

        if (!validSecret(suppliedSecret)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("status", 401, "error", "Unauthorized", "message", "Invalid internal secret"));
        }

        String key = idempotencyKey == null || idempotencyKey.isBlank()
                ? "TATUM-DEP-" + request.userId() + "-" + request.currency().trim().toUpperCase() + "-" + request.network().trim().toUpperCase()
                : idempotencyKey.trim();

        try {
            UUID userId = UUID.fromString(request.userId());

            CustodyOperation operation = custodyProviderService.requestDepositAddress(
                    userId, request.currency(), request.network(), key);

            DepositAddressDtos.DepositAddressResponse address = depositAddressService.assign(
                    new DepositAddressDtos.AssignAddressRequest(
                            request.userId(),
                            request.currency(),
                            request.network(),
                            operation.getProviderAddress(),
                            operation.getMemoTag()));

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("status", 200);
            response.put("provider", "TATUM");
            response.put("operationId", operation.getId());
            response.put("providerReference", operation.getProviderReference());
            response.put("depositAddress", address);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of(
                            "status", 502,
                            "error", "Bad Gateway",
                            "provider", "TATUM",
                            "message", sanitize(e.getMessage())));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "status", 500,
                            "error", "Internal Server Error",
                            "provider", "TATUM",
                            "message", "Unable to create deposit address"));
        }
    }

    private boolean validSecret(String suppliedSecret) {
        return suppliedSecret != null
                && internalSecret != null
                && MessageDigest.isEqual(
                    internalSecret.getBytes(StandardCharsets.UTF_8),
                    suppliedSecret.getBytes(StandardCharsets.UTF_8));
    }

    private String sanitize(String message) {
        if (message == null || message.isBlank()) return "Tatum deposit address creation failed";
        return message.length() > 1000 ? message.substring(0, 1000) : message;
    }

    public record CreateDepositAddressRequest(
            @NotBlank String userId,
            @NotBlank String currency,
            @NotBlank String network) {}
}
