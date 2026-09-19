package com.oakpay.auth.admin;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import java.util.List;
import java.util.UUID;
@RestController @RequestMapping("/api/v1/admin/queries") @PreAuthorize("hasRole('ADMIN')")
public class ClientQueryController {
    private final ClientQueryService service; private final AuditLogService audit;
    public ClientQueryController(ClientQueryService service, AuditLogService audit){this.service=service;this.audit=audit;}
    @GetMapping public ResponseEntity<List<ClientQuery>> getQueries(@RequestParam(required=false) String status){return ResponseEntity.ok(service.getQueries(status));}
    @GetMapping("/{id}") public ResponseEntity<ClientQuery> getQuery(@PathVariable UUID id){return ResponseEntity.ok(service.getQuery(id));}
    @PostMapping("/{id}/assign") public ResponseEntity<ClientQuery> assignQuery(@PathVariable UUID id,@RequestBody AssignQueryRequest request,Authentication auth){
        ClientQuery result=service.assignQuery(id,request.adminUserId());
        audit.record(actor(auth),"ADMIN","CLIENT_QUERY_ASSIGNED","CLIENT_QUERY",id.toString(),"SUCCESS",null,"{\"assignedAdminId\":\""+request.adminUserId()+"\"}");
        return ResponseEntity.ok(result);
    }
    @PostMapping("/{id}/resolve") public ResponseEntity<ClientQuery> resolveQuery(@PathVariable UUID id,@RequestBody ResolveQueryRequest request,Authentication auth){
        ClientQuery result=service.resolveQuery(id,request.resolution());
        audit.record(actor(auth),"ADMIN","CLIENT_QUERY_RESOLVED","CLIENT_QUERY",id.toString(),"SUCCESS",null,null);
        return ResponseEntity.ok(result);
    }
    private UUID actor(Authentication a){return UUID.fromString(a.getName());}
    public record AssignQueryRequest(UUID adminUserId){} public record ResolveQueryRequest(String resolution){}
}