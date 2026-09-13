package com.oakpay.auth.admin;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ClientQueryService {

    private final ClientQueryRepository repository;

    public ClientQueryService(ClientQueryRepository repository) {
        this.repository = repository;
    }

    public List<ClientQuery> getQueries(String status) {

        if (status == null || status.isBlank()) {
            return repository.findAllByOrderByCreatedAtDesc();
        }

        return repository.findByStatusOrderByCreatedAtDesc(
                status.trim().toUpperCase()
        );
    }

    public ClientQuery getQuery(UUID id) {

        return repository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Client query not found: " + id
                        )
                );
    }

    public ClientQuery assignQuery(
            UUID queryId,
            UUID adminUserId
    ) {

        if (adminUserId == null) {
            throw new IllegalArgumentException(
                    "Admin user ID is required."
            );
        }

        ClientQuery query = getQuery(queryId);

        if ("RESOLVED".equalsIgnoreCase(query.getStatus())) {
            throw new IllegalStateException(
                    "A resolved query cannot be assigned."
            );
        }

        query.setAssignedAdminId(adminUserId);

        if (!"ESCALATED".equalsIgnoreCase(query.getStatus())) {
            query.setStatus("ASSIGNED");
        }

        query.setUpdatedAt(LocalDateTime.now());

        return repository.save(query);
    }

    public ClientQuery resolveQuery(
            UUID queryId,
            String resolution
    ) {

        if (resolution == null || resolution.isBlank()) {
            throw new IllegalArgumentException(
                    "Resolution is required."
            );
        }

        ClientQuery query = getQuery(queryId);

        if ("RESOLVED".equalsIgnoreCase(query.getStatus())) {
            throw new IllegalStateException(
                    "This query has already been resolved."
            );
        }

        LocalDateTime now = LocalDateTime.now();

        query.setResolution(resolution.trim());
        query.setStatus("RESOLVED");
        query.setResolvedAt(now);
        query.setUpdatedAt(now);

        return repository.save(query);
    }
}