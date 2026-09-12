package com.oakpay.auth.api;

import com.oakpay.auth.security.UserPrincipal;
import com.oakpay.auth.service.TwoFactorService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth/2fa")
public class TwoFactorController {
    private final TwoFactorService twoFactorService;
    public TwoFactorController(TwoFactorService twoFactorService){this.twoFactorService=twoFactorService;}
    @GetMapping("/status") public TwoFactorDtos.StatusResponse status(@AuthenticationPrincipal UserPrincipal principal){return twoFactorService.status(principal.getUserId());}
    @PostMapping("/setup") public TwoFactorDtos.SetupResponse setup(@AuthenticationPrincipal UserPrincipal principal){return twoFactorService.setup(principal.getUserId());}
    @PostMapping("/enable") public TwoFactorDtos.EnabledResponse enable(@AuthenticationPrincipal UserPrincipal principal,@Valid @RequestBody TwoFactorDtos.CodeRequest request){return twoFactorService.enable(principal.getUserId(),request.code());}
    @PostMapping("/disable") public TwoFactorDtos.EnabledResponse disable(@AuthenticationPrincipal UserPrincipal principal,@Valid @RequestBody TwoFactorDtos.DisableRequest request){return twoFactorService.disable(principal.getUserId(),request.password(),request.code());}
}
