package com.oakpay.trading.p2p;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/internal/admin/dashboard")
public class InternalAdminDashboardController {

    private final P2PDisputeService disputeService;
    private final String internalSecret;

    public InternalAdminDashboardController(
            P2PDisputeService disputeService,
            @Value("${oakpay.internal-secret}") String internalSecret) {
        this.disputeService = disputeService;
        this.internalSecret = internalSecret;
    }

    @GetMapping("/disputes")
    public DashboardDisputeCounts disputeCounts(
            @RequestHeader(value = "X-OakPay-Internal-Secret", required = false) String suppliedSecret) {
        requireInternalSecret(suppliedSecret);
        long open = disputeService.openDisputes().size();
        return new DashboardDisputeCounts(open, open);
    }

    private void requireInternalSecret(String suppliedSecret) {
        if (suppliedSecret == null || internalSecret == null || !suppliedSecret.equals(internalSecret)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Internal access required");
        }
    }

    public record DashboardDisputeCounts(long activeDisputes, long pendingResolutions) {}
}
