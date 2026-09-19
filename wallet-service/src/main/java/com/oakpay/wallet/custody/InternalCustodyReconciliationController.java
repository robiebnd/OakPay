package com.oakpay.wallet.custody;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/wallets/custody/reconciliation")
public class InternalCustodyReconciliationController {
    private final CustodyReconciliationService reconciliationService;
    private final String internalSecret;

    public InternalCustodyReconciliationController(
            CustodyReconciliationService reconciliationService,
            @Value("${oakpay.internal-secret}") String internalSecret) {
        this.reconciliationService = reconciliationService;
        this.internalSecret = internalSecret;
    }

    @GetMapping("/withdrawals/pending")
    public List<CustodyWithdrawalDtos.WithdrawalResponse> pendingWithdrawals(
            @RequestParam(defaultValue = "5") int olderThanMinutes,
            @RequestHeader(value = "X-OakPay-Internal-Secret", required = false) String suppliedSecret) {
        requireInternalSecret(suppliedSecret);
        int minutes = Math.max(1, Math.min(olderThanMinutes, 1440));
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(minutes);
        return reconciliationService.findPendingWithdrawals(cutoff).stream()
                .map(CustodyWithdrawalDtos.WithdrawalResponse::from)
                .toList();
    }

    @PostMapping("/withdrawals/{operationId}")
    public CustodyWithdrawalDtos.WithdrawalResponse reconcileWithdrawal(
            @PathVariable UUID operationId,
            @RequestHeader(value = "X-OakPay-Internal-Secret", required = false) String suppliedSecret) {
        requireInternalSecret(suppliedSecret);
        CustodyOperation operation = reconciliationService.reconcileWithdrawal(operationId);
        return CustodyWithdrawalDtos.WithdrawalResponse.from(operation);
    }

    private void requireInternalSecret(String suppliedSecret) {
        if (suppliedSecret == null || internalSecret == null || !MessageDigest.isEqual(
                internalSecret.getBytes(StandardCharsets.UTF_8),
                suppliedSecret.getBytes(StandardCharsets.UTF_8))) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Invalid internal secret");
        }
    }
}
