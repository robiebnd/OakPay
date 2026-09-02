package com.oakpay.trading.p2p;

import com.oakpay.trading.wallet.WalletClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class P2PDisputeService {
    private final P2PDisputeRepository disputeRepository;
    private final P2PDisputeAuditRepository auditRepository;
    private final P2PTradeRepository tradeRepository;
    private final WalletClient walletClient;
    private final String adminSecret;

    public P2PDisputeService(P2PDisputeRepository disputeRepository,
                             P2PDisputeAuditRepository auditRepository,
                             P2PTradeRepository tradeRepository,
                             WalletClient walletClient,
                             @org.springframework.beans.factory.annotation.Value("${oakpay.admin.dispute-secret}") String adminSecret) {
        this.disputeRepository = disputeRepository;
        this.auditRepository = auditRepository;
        this.tradeRepository = tradeRepository;
        this.walletClient = walletClient;
        this.adminSecret = adminSecret;
    }

    @Transactional
    public P2PDisputeDtos.DisputeResponse open(UUID userId, UUID tradeId, P2PDisputeDtos.OpenRequest request) {
        P2PTrade trade = tradeRepository.findById(tradeId)
                .orElseThrow(() -> new IllegalArgumentException("P2P trade not found"));
        if (!trade.getBuyerId().equals(userId) && !trade.getSellerId().equals(userId))
            throw new IllegalArgumentException("Trade does not belong to user");
        if (trade.getStatus() != P2PTradeStatus.PAYMENT_MARKED)
            throw new IllegalStateException("Only a payment-marked trade can be disputed");
        if (request == null || request.reason() == null || request.reason().isBlank())
            throw new IllegalArgumentException("Dispute reason is required");
        if (disputeRepository.findByTradeId(tradeId).isPresent())
            throw new IllegalStateException("A dispute already exists for this trade");

        P2PDispute dispute = new P2PDispute();
        dispute.setTradeId(tradeId);
        dispute.setOpenedBy(userId);
        dispute.setReason(request.reason().trim());
        dispute.setEvidence(request.evidence());
        dispute.setStatus(DisputeStatus.OPEN);
        dispute = disputeRepository.save(dispute);

        trade.setStatus(P2PTradeStatus.DISPUTED);
        tradeRepository.save(trade);
        audit(dispute, userId, "DISPUTE_OPENED", request.reason());
        return P2PDisputeDtos.DisputeResponse.from(dispute);
    }

    @Transactional(readOnly = true)
    public List<P2PDisputeDtos.DisputeResponse> mine(UUID userId) {
        return disputeRepository.findAllByOpenedByOrderByCreatedAtDesc(userId).stream()
                .map(P2PDisputeDtos.DisputeResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<P2PDisputeDtos.DisputeResponse> openDisputes(String secret) {
        requireAdmin(secret);
        return disputeRepository.findAllByStatusOrderByCreatedAtAsc(DisputeStatus.OPEN).stream()
                .map(P2PDisputeDtos.DisputeResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<P2PDisputeDtos.AuditResponse> audit(UUID disputeId, String secret) {
        requireAdmin(secret);
        return auditRepository.findAllByDisputeIdOrderByCreatedAtAsc(disputeId).stream()
                .map(P2PDisputeDtos.AuditResponse::from).toList();
    }

    @Transactional
    public P2PDisputeDtos.DisputeResponse resolve(UUID adminId, UUID disputeId,
                                                   P2PDisputeDtos.ResolveRequest request, String secret) {
        requireAdmin(secret);
        if (request == null || request.resolution() == null)
            throw new IllegalArgumentException("Resolution is required");
        P2PDispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new IllegalArgumentException("Dispute not found"));
        if (dispute.getStatus() != DisputeStatus.OPEN)
            throw new IllegalStateException("Dispute is already resolved");

        P2PTrade trade = tradeRepository.findById(dispute.getTradeId())
                .orElseThrow(() -> new IllegalArgumentException("P2P trade not found"));
        if (trade.getStatus() != P2PTradeStatus.DISPUTED)
            throw new IllegalStateException("Trade is not in dispute status");

        if (request.resolution() == DisputeResolution.BUYER_WINS) {
            walletClient.releaseEscrow(trade.getSellerId(), trade.getBuyerId(), trade.getAsset(), trade.getQuantity(), trade.getId());
            trade.setStatus(P2PTradeStatus.COMPLETED);
        } else {
            walletClient.unlock(trade.getSellerId(), trade.getAsset(), trade.getQuantity(), trade.getId());
            trade.setStatus(P2PTradeStatus.CANCELLED);
        }
        tradeRepository.save(trade);

        dispute.setResolution(request.resolution());
        dispute.setResolutionNote(request.note());
        dispute.setResolvedBy(adminId);
        dispute.setResolvedAt(LocalDateTime.now());
        dispute.setStatus(DisputeStatus.RESOLVED);
        disputeRepository.save(dispute);
        audit(dispute, adminId, "DISPUTE_RESOLVED", request.resolution().name() + ": " + request.note());
        return P2PDisputeDtos.DisputeResponse.from(dispute);
    }

    private void audit(P2PDispute dispute, UUID actorId, String eventType, String note) {
        P2PDisputeAudit entry = new P2PDisputeAudit();
        entry.setDisputeId(dispute.getId());
        entry.setTradeId(dispute.getTradeId());
        entry.setActorId(actorId);
        entry.setEventType(eventType);
        entry.setNote(note);
        auditRepository.save(entry);
    }

    private void requireAdmin(String supplied) {
        if (supplied == null || adminSecret == null || !MessageDigest.isEqual(
                supplied.getBytes(StandardCharsets.UTF_8), adminSecret.getBytes(StandardCharsets.UTF_8)))
            throw new SecurityException("Invalid admin credentials");
    }
}
