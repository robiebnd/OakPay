package com.oakpay.auth.api;

import com.oakpay.auth.security.UserPrincipal;
import com.oakpay.auth.service.KycService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/kyc")
public class KycController {
    private final KycService kycService;
    public KycController(KycService kycService) { this.kycService = kycService; }

    @GetMapping
    public KycDtos.KycResponse get(@AuthenticationPrincipal UserPrincipal principal) {
        return kycService.get(principal.getUserId());
    }

    @PostMapping("/documents")
    public ResponseEntity<KycDtos.DocumentResponse> addDocument(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody KycDtos.DocumentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(kycService.addDocument(principal.getUserId(), request));
    }

    @PostMapping("/submit")
    public KycDtos.KycResponse submit(@AuthenticationPrincipal UserPrincipal principal) {
        return kycService.submit(principal.getUserId());
    }
}
