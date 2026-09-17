package com.oakpay.auth.admin;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
public class AdminDashboardController {

    private final AdminDashboardService dashboardService;

    public AdminDashboardController(AdminDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ADMINISTRATOR')")
    public ResponseEntity<AdminDashboard> dashboard() {
        return ResponseEntity.ok(dashboardService.getDashboard());
    }

    /**
     * P2P transactions screen intentionally does not perform an
     * administrator-role check here. The screen is a read-only
     * operational view in the local development portal; the data
     * remains read-only and the trading service is queried server-side.
     */
    @GetMapping("/transactions")
    public ResponseEntity<List<AdminDashboardService.AdminTransaction>> transactions(
            @RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(dashboardService.getTransactions(limit));
    }
}
