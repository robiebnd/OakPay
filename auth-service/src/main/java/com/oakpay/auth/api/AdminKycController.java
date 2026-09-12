package com.oakpay.auth.api;

import com.oakpay.auth.service.AdminKycService;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/kyc")
public class AdminKycController {
    private final AdminKycService service;
    public AdminKycController(AdminKycService service){this.service=service;}
    @GetMapping("/summary") public AdminKycDtos.QueueSummary summary(){return service.summary();}
    @GetMapping public List<AdminKycDtos.QueueItem> queue(@RequestParam(defaultValue="PENDING") String status){return service.queue(status);}
    @GetMapping("/{id}") public AdminKycDtos.QueueItem get(@PathVariable UUID id){return service.get(id);}
    @PostMapping("/{id}/approve") public AdminKycDtos.DecisionResponse approve(@PathVariable UUID id){return service.approve(id);}
    @PostMapping("/{id}/reject") public AdminKycDtos.DecisionResponse reject(@PathVariable UUID id,@RequestBody AdminKycDtos.DecisionRequest request){return service.reject(id,request.reason());}
}