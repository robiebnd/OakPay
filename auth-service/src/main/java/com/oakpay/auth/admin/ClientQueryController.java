package com.oakpay.auth.admin;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/queries")
@PreAuthorize("hasRole('ADMIN')")
public class ClientQueryController {

    private final ClientQueryService service;

    public ClientQueryController(ClientQueryService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<ClientQuery>> getQueries(
            @RequestParam(required = false) String status
    ) {
        return ResponseEntity.ok(
                service.getQueries(status)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientQuery> getQuery(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(
                service.getQuery(id)
        );
    }

    @PostMapping("/{id}/assign")
    public ResponseEntity<ClientQuery> assignQuery(
            @PathVariable UUID id,
            @RequestBody AssignQueryRequest request
    ) {
        return ResponseEntity.ok(
                service.assignQuery(
                        id,
                        request.adminUserId()
                )
        );
    }

    @PostMapping("/{id}/resolve")
    public ResponseEntity<ClientQuery> resolveQuery(
            @PathVariable UUID id,
            @RequestBody ResolveQueryRequest request
    ) {
        return ResponseEntity.ok(
                service.resolveQuery(
                        id,
                        request.resolution()
                )
        );
    }

    public record AssignQueryRequest(
            UUID adminUserId
    ) {
    }

    public record ResolveQueryRequest(
            String resolution
    ) {
    }
}