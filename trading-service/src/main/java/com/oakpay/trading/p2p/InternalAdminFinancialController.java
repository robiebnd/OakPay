package com.oakpay.trading.p2p;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/internal/admin/finance")
public class InternalAdminFinancialController {
    private final TradeRepository tradeRepository;
    private final P2PCommissionRepository commissionRepository;
    private final PlatformFeeService platformFeeService;
    private final P2PCommissionService commissionService;
    private final String internalSecret;

    public InternalAdminFinancialController(
            TradeRepository tradeRepository,
            P2PCommissionRepository commissionRepository,
            PlatformFeeService platformFeeService,
            P2PCommissionService commissionService,
            @Value("$" + "{oakpay.internal-secret}") String internalSecret) {
        this.tradeRepository = tradeRepository;
        this.commissionRepository = commissionRepository;
        this.platformFeeService = platformFeeService;
        this.commissionService = commissionService;
        this.internalSecret = internalSecret;
    }

    @GetMapping
    public FinancialSummary summary(
            @RequestHeader(value = "X-OakPay-Internal-Secret", required = false) String suppliedSecret) {
        requireInternalSecret(suppliedSecret);

        Map<String, SpotFeeSummary> spotByCurrency = new LinkedHashMap<>();
        tradeRepository.findAll().forEach(trade -> {
            String currency = normalize(trade.getQuoteCurrency());
            SpotFeeSummary current = spotByCurrency.computeIfAbsent(
                    currency, key -> new SpotFeeSummary(key, 0, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));
            spotByCurrency.put(currency, current.add(
                    trade.getGrossValue(),
                    trade.getBuyerFee(),
                    trade.getSellerFee()));
        });

        Map<String, P2PCommissionSummary> p2pByCurrency = new LinkedHashMap<>();
        commissionRepository.findAll().forEach(commission -> {
            String currency = normalize(commission.getFiatCurrency());
            P2PCommissionSummary current = p2pByCurrency.computeIfAbsent(
                    currency, key -> new P2PCommissionSummary(key, 0, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));
            BigDecimal amount = money(commission.getCommissionAmount());
            BigDecimal assessed = current.assessed().add(amount);
            BigDecimal collected = current.collected();
            BigDecimal waived = current.waived();
            if (commission.getStatus() == P2PCommissionStatus.COLLECTED) {
                collected = collected.add(amount);
            }
            if (commission.getStatus() == P2PCommissionStatus.WAIVED) {
                waived = waived.add(amount);
            }
            BigDecimal outstanding = assessed.subtract(collected).subtract(waived).max(BigDecimal.ZERO);
            p2pByCurrency.put(currency, new P2PCommissionSummary(
                    currency,
                    current.commissionCount() + 1,
                    assessed,
                    collected,
                    outstanding,
                    waived
            ));
        });

        long executedTrades = tradeRepository.count();
        long commissionRecords = commissionRepository.count();
        long collectedCommissionRecords = commissionRepository.findAll().stream()
                .filter(c -> c.getStatus() == P2PCommissionStatus.COLLECTED)
                .count();

        return new FinancialSummary(
                platformFeeService.current(),
                executedTrades,
                commissionRecords,
                collectedCommissionRecords,
                new ArrayList<>(spotByCurrency.values()),
                new ArrayList<>(p2pByCurrency.values())
        );
    }

    @GetMapping("/commissions")
    public List<P2PCommissionDtos.Response> commissions(
            @RequestParam(required = false) P2PCommissionStatus status,
            @RequestParam(defaultValue = "200") int limit,
            @RequestHeader(value = "X-OakPay-Internal-Secret", required = false) String suppliedSecret) {
        requireInternalSecret(suppliedSecret);
        int safeLimit = Math.min(Math.max(limit, 1), 200);
        var page = org.springframework.data.domain.PageRequest.of(
                0, safeLimit,
                org.springframework.data.domain.Sort.by(
                        org.springframework.data.domain.Sort.Direction.DESC, "createdAt"));
        List<P2PCommission> rows = status == null
                ? commissionRepository.findAll(page).getContent()
                : commissionRepository.findAllByStatusOrderByCreatedAtDesc(status, page);
        return rows.stream().map(P2PCommissionDtos.Response::from).toList();
    }

    @PostMapping("/commissions/{tradeId}/collect")
    public P2PCommissionDtos.Response collectCommission(
            @PathVariable UUID tradeId,
            @RequestBody P2PCommissionDtos.CollectionRequest request,
            @RequestHeader(value = "X-OakPay-Internal-Secret", required = false) String suppliedSecret) {
        requireInternalSecret(suppliedSecret);
        return P2PCommissionDtos.Response.from(commissionService.collect(tradeId, request));
    }

    @PatchMapping("/fees/{key}")
    public Map<String, BigDecimal> updateFee(
            @PathVariable String key,
            @RequestParam BigDecimal value,
            @RequestHeader(value = "X-OakPay-Internal-Secret", required = false) String suppliedSecret,
            @RequestHeader(value = "X-OakPay-Admin-Actor", required = false) String actor) {
        requireInternalSecret(suppliedSecret);
        UUID actorId = null;
        if (actor != null && !actor.isBlank()) {
            try {
                actorId = UUID.fromString(actor.trim());
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid admin actor");
            }
        }
        String normalizedKey = key.trim().toUpperCase(Locale.ROOT);
        return Map.of(normalizedKey, platformFeeService.set(normalizedKey, value, actorId));
    }

    private void requireInternalSecret(String suppliedSecret) {
        if (suppliedSecret == null || internalSecret == null ||
                !MessageDigest.isEqual(
                        internalSecret.getBytes(StandardCharsets.UTF_8),
                        suppliedSecret.getBytes(StandardCharsets.UTF_8))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Internal access required");
        }
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }

    private BigDecimal money(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value.setScale(2, RoundingMode.HALF_UP);
    }

    public record FinancialSummary(
            Map<String, BigDecimal> currentFees,
            long executedTrades,
            long commissionRecords,
            long collectedCommissionRecords,
            List<SpotFeeSummary> spotFees,
            List<P2PCommissionSummary> p2pCommissions) {}

    public record SpotFeeSummary(
            String quoteCurrency,
            long tradeCount,
            BigDecimal grossVolume,
            BigDecimal buyerFees,
            BigDecimal sellerFees) {
        SpotFeeSummary add(BigDecimal gross, BigDecimal buyerFee, BigDecimal sellerFee) {
            return new SpotFeeSummary(
                    quoteCurrency,
                    tradeCount + 1,
                    grossVolume.add(money(gross)),
                    buyerFees.add(money(buyerFee)),
                    sellerFees.add(money(sellerFee)));
        }
        public BigDecimal totalFees() { return buyerFees.add(sellerFees); }
    }

    public record P2PCommissionSummary(
            String fiatCurrency,
            long commissionCount,
            BigDecimal assessed,
            BigDecimal collected,
            BigDecimal outstanding,
            BigDecimal waived) {}
}
