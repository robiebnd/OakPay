package com.oakpay.auth.api;
import com.oakpay.auth.admin.AuditLogService;
import com.oakpay.auth.service.AdminKycService;
import java.util.List; import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/admin/kyc")
public class AdminKycController {
    private final AdminKycService service; private final AuditLogService audit;
    public AdminKycController(AdminKycService service, AuditLogService audit){this.service=service;this.audit=audit;}
    @GetMapping("/summary") public AdminKycDtos.QueueSummary summary(){return service.summary();}
    @GetMapping public List<AdminKycDtos.QueueItem> queue(@RequestParam(defaultValue="PENDING") String status){return service.queue(status);}
    @GetMapping("/{id}") public AdminKycDtos.QueueItem get(@PathVariable UUID id){return service.get(id);}
    @PostMapping("/{id}/approve") public AdminKycDtos.DecisionResponse approve(@PathVariable UUID id,Authentication auth){var result=service.approve(id);audit.record(UUID.fromString(auth.getName()),"ADMIN","KYC_APPROVED","KYC",id.toString(),"SUCCESS",null,null);return result;}
    @PostMapping("/{id}/reject") public AdminKycDtos.DecisionResponse reject(@PathVariable UUID id,@RequestBody AdminKycDtos.DecisionRequest request,Authentication auth){var result=service.reject(id,request.reason());audit.record(UUID.fromString(auth.getName()),"ADMIN","KYC_REJECTED","KYC",id.toString(),"SUCCESS",null,null);return result;}
}