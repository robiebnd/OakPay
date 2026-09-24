package com.oakpay.wallet.custody;

import com.oakpay.wallet.deposit.DepositDtos;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/wallets/custody/reconciliation")
public class CustodyDepositReconciliationController {

    private final CustodyReconciliationService reconciliationService;
    private final String internalSecret;

    public CustodyDepositReconciliationController(
            CustodyReconciliationService reconciliationService,
            @Value("${oakpay.internal-secret}") String internalSecret) {
        this.reconciliationService = reconciliationService;
        this.internalSecret = internalSecret;
    }

    @PostMapping("/deposits/discover")
    public java.util.List<DepositDtos.DepositResponse> discoverDeposits(
            @RequestHeader(value = "X-OakPay-Internal-Secret", required = false) String suppliedSecret,
            @RequestParam String currency,
            @RequestParam String network,
            @RequestParam String address) {

        if (suppliedSecret == null || !suppliedSecret.equals(internalSecret)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid internal secret");
        }
        return reconciliationService.discoverDeposits(currency, network, address);
    }

    @PostMapping("/deposits/{depositId}")
    public DepositDtos.DepositResponse reconcileDeposit(
            @RequestHeader(value = "X-OakPay-Internal-Secret", required = false) String suppliedSecret,
            @PathVariable UUID depositId) {

        if (suppliedSecret == null || !suppliedSecret.equals(internalSecret)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid internal secret");
        }

        return reconciliationService.reconcileDeposit(depositId);
    }
}
