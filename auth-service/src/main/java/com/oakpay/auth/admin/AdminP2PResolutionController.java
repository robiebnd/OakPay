package com.oakpay.auth.admin;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/resolution-centre")
@PreAuthorize("hasAnyRole('ADMIN', 'ADMINISTRATOR')")
public class AdminP2PResolutionController {

    private final RestClient tradingClient;
    private final String disputeSecret;

    public AdminP2PResolutionController(
            RestClient.Builder restClientBuilder,
            @Value("${oakpay.trading.base-url:http://localhost:8085}") String tradingBaseUrl,
            @Value("${oakpay.admin.dispute-secret}") String disputeSecret) {
        this.tradingClient = restClientBuilder.baseUrl(tradingBaseUrl).build();
        this.disputeSecret = disputeSecret;
    }

    @GetMapping("/disputes")
    public ResponseEntity<JsonNode> disputes() {
        return ResponseEntity.ok(tradingGet("/api/v1/p2p/admin/disputes"));
    }

    @GetMapping("/disputes/{disputeId}/audit")
    public ResponseEntity<JsonNode> audit(@PathVariable UUID disputeId) {
        return ResponseEntity.ok(tradingGet(
                "/api/v1/p2p/admin/disputes/" + disputeId + "/audit"));
    }

    @PostMapping("/disputes/{disputeId}/resolve")
    public ResponseEntity<JsonNode> resolve(
            @PathVariable UUID disputeId,
            @RequestBody JsonNode request) {
        if (request == null || !request.isObject()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A dispute resolution request is required");
        }

        JsonNode result = tradingClient.post()
                .uri("/api/v1/p2p/admin/disputes/" + disputeId + "/resolve")
                .header("X-OakPay-Admin-Secret", disputeSecret)
                .header(HttpHeaders.ACCEPT, "application/json")
                .body(request)
                .retrieve()
                .body(JsonNode.class);

        return ResponseEntity.ok(result);
    }

    private JsonNode tradingGet(String path) {
        JsonNode result = tradingClient.get()
                .uri(path)
                .header("X-OakPay-Admin-Secret", disputeSecret)
                .header(HttpHeaders.ACCEPT, "application/json")
                .retrieve()
                .body(JsonNode.class);

        return result == null ? com.fasterxml.jackson.databind.node.JsonNodeFactory.instance.arrayNode() : result;
    }
}
