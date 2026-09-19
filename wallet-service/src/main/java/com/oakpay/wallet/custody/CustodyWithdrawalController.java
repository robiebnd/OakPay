package com.oakpay.wallet.custody;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/wallets/withdrawals")
public class CustodyWithdrawalController {
    private final CustodyWithdrawalService withdrawalService;

    public CustodyWithdrawalController(CustodyWithdrawalService withdrawalService) {
        this.withdrawalService = withdrawalService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public CustodyWithdrawalDtos.WithdrawalResponse submit(
            @Valid @RequestBody CustodyWithdrawalDtos.WithdrawalRequest request,
            Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        CustodyOperation operation = withdrawalService.submit(
                userId,
                request.currency(),
                request.network(),
                request.amount(),
                request.destinationAddress(),
                request.memoTag(),
                request.idempotencyKey());
        return CustodyWithdrawalDtos.WithdrawalResponse.from(operation);
    }
}
