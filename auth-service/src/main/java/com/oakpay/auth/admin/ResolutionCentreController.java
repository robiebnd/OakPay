package com.oakpay.auth.admin;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/resolution-centre")
@PreAuthorize("hasAnyRole('ADMIN', 'ADMINISTRATOR')")
public class ResolutionCentreController {
    private final ResolutionCentreService service;
    private final AuditLogService audit;
    public ResolutionCentreController(ResolutionCentreService service, AuditLogService audit){this.service=service;this.audit=audit;}
    @GetMapping("/disputes") public ResponseEntity<List<ResolutionCentreService.DisputeResponse>> disputes(@RequestHeader(value="Authorization",required=false) String authorization){return ResponseEntity.ok(service.disputes(authorization));}
    @GetMapping("/disputes/{disputeId}") public ResponseEntity<ResolutionCentreService.DisputeResponse> dispute(@PathVariable UUID disputeId,@RequestHeader(value="Authorization",required=false) String authorization){return ResponseEntity.ok(service.dispute(disputeId,authorization));}
    @GetMapping("/disputes/{disputeId}/audit") public ResponseEntity<List<ResolutionCentreService.AuditResponse>> audit(@PathVariable UUID disputeId,@RequestHeader(value="Authorization",required=false) String authorization){return ResponseEntity.ok(service.audit(disputeId,authorization));}
    @PostMapping("/disputes/{disputeId}/resolve")
    public ResponseEntity<ResolutionCentreService.DisputeResponse> resolve(@PathVariable UUID disputeId,@RequestBody ResolutionCentreService.ResolveRequest request,@RequestHeader(value="Authorization",required=false) String authorization,Authentication authentication){
        UUID actor=UUID.fromString(authentication.getName());
        ResponseEntity<ResolutionCentreService.DisputeResponse> result=ResponseEntity.ok(service.resolve(disputeId,request,authorization,actor));
        audit.record(actor,"ADMIN","P2P_DISPUTE_RESOLVED","P2P_DISPUTE",disputeId.toString(),"SUCCESS",null,request.resolution().name());
        return result;
    }
}