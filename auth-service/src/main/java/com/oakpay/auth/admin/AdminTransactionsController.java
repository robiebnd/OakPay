package com.oakpay.auth.admin;

import com.oakpay.auth.security.UserPrincipal;
import com.oakpay.auth.user.User;
import com.oakpay.auth.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/transactions")
public class AdminTransactionsController {

    private final AdminDashboardService dashboardService;
    private final UserRepository userRepository;

    public AdminTransactionsController(
            AdminDashboardService dashboardService,
            UserRepository userRepository) {
        this.dashboardService = dashboardService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<List<AdminDashboardService.AdminTransaction>> transactions(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "100") int limit) {

        requireAdmin(principal);
        return ResponseEntity.ok(dashboardService.getTransactions(limit));
    }

    private void requireAdmin(UserPrincipal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }

        User user = userRepository.findById(principal.getUserId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Administrator account not found"));

        String role = user.getRole() == null ? "" : user.getRole().trim();
        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Administrator access required");
        }
    }
}
