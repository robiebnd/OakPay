package com.oakpay.auth.admin;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {
    private final AdminUserService service;
    private final AuditLogService auditLogService;
    public AdminUserController(AdminUserService service, AuditLogService auditLogService) { this.service = service; this.auditLogService = auditLogService; }
    @GetMapping public ResponseEntity<List<AdminUserDtos.UserResponse>> getUsers(@RequestParam(required=false) String status,@RequestParam(required=false) String role){return ResponseEntity.ok(service.getUsers(status,role));}
    @GetMapping("/{id}") public ResponseEntity<AdminUserDtos.UserResponse> getUser(@PathVariable UUID id){return ResponseEntity.ok(service.getUser(id));}
    @PatchMapping("/{id}/status")
    public ResponseEntity<AdminUserDtos.UserResponse> updateStatus(@PathVariable UUID id,@RequestBody AdminUserDtos.UpdateStatusRequest request,Authentication authentication,HttpServletRequest httpRequest){
        AdminUserDtos.UserResponse response=service.updateStatus(id,request.status());
        UUID actor=UUID.fromString(authentication.getName());
        String action="ACTIVE".equals(response.status())?"USER_ACTIVATED":"USER_DEACTIVATED";
        auditLogService.record(actor,"ADMIN",action,"USER",id.toString(),"SUCCESS",httpRequest.getRemoteAddr(),"{\"status\":\""+response.status()+"\"}");
        return ResponseEntity.ok(response);
    }
}