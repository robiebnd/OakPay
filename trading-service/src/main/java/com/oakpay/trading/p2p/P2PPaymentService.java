package com.oakpay.trading.p2p;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class P2PPaymentService {
    private final P2PPaymentRepository paymentRepository;
    private final P2PTradeRepository tradeRepository;

    public P2PPaymentService(P2PPaymentRepository paymentRepository, P2PTradeRepository tradeRepository) {
        this.paymentRepository = paymentRepository;
        this.tradeRepository = tradeRepository;
    }

    @Transactional
    public P2PPaymentDtos.PaymentResponse submit(UUID buyerId, UUID tradeId, P2PPaymentDtos.SubmitRequest request) {
        P2PTrade trade = tradeRepository.findById(tradeId)
                .orElseThrow(() -> new IllegalArgumentException("P2P trade not found"));
        if (!trade.getBuyerId().equals(buyerId)) throw new IllegalArgumentException("Trade does not belong to buyer");
        if (trade.getExpiresAt().isBefore(LocalDateTime.now())) throw new IllegalStateException("P2P trade has expired");
        if (trade.getStatus() == P2PTradeStatus.PAYMENT_MARKED) {
            return paymentRepository.findByTradeId(tradeId)
                    .map(P2PPaymentDtos.PaymentResponse::from)
                    .orElseThrow(() -> new IllegalStateException("Payment record not found"));
        }
        if (trade.getStatus() != P2PTradeStatus.PAYMENT_PENDING)
            throw new IllegalStateException("Trade is not awaiting payment");
        if (request == null || request.paymentReference() == null || request.paymentReference().isBlank())
            throw new IllegalArgumentException("Payment reference is required");
        if (paymentRepository.findByTradeId(tradeId).isPresent())
            throw new IllegalStateException("Payment has already been submitted for this trade");

        P2PPayment payment = new P2PPayment();
        payment.setTradeId(tradeId);
        payment.setPayerId(trade.getBuyerId());
        payment.setPayeeId(trade.getSellerId());
        payment.setAmount(trade.getFiatAmount());
        payment.setCurrency(trade.getFiatCurrency());
        payment.setPaymentMethod(trade.getPaymentMethod());
        payment.setPaymentReference(request.paymentReference().trim());
        payment.setNote(request.note());
        payment.setStatus(PaymentStatus.SUBMITTED);
        payment.setSubmittedAt(LocalDateTime.now());

        trade.setPaymentReference(payment.getPaymentReference());
        trade.setPaymentNote(payment.getNote());
        trade.setStatus(P2PTradeStatus.PAYMENT_MARKED);
        tradeRepository.save(trade);

        return P2PPaymentDtos.PaymentResponse.from(paymentRepository.save(payment));
    }

    @Transactional(readOnly = true)
    public P2PPaymentDtos.PaymentResponse getForTrade(UUID userId, UUID tradeId) {
        P2PTrade trade = tradeRepository.findById(tradeId)
                .orElseThrow(() -> new IllegalArgumentException("P2P trade not found"));
        if (!trade.getBuyerId().equals(userId) && !trade.getSellerId().equals(userId))
            throw new IllegalArgumentException("Trade does not belong to user");
        P2PPayment payment = paymentRepository.findByTradeId(tradeId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));
        return P2PPaymentDtos.PaymentResponse.from(payment);
    }

    @Transactional
    public P2PPaymentDtos.PaymentResponse verify(UUID sellerId, UUID tradeId) {
        P2PTrade trade = tradeRepository.findById(tradeId)
                .orElseThrow(() -> new IllegalArgumentException("P2P trade not found"));
        if (!trade.getSellerId().equals(sellerId)) throw new IllegalArgumentException("Trade does not belong to seller");
        if (trade.getStatus() == P2PTradeStatus.COMPLETED) {
            return paymentRepository.findByTradeId(tradeId)
                    .map(P2PPaymentDtos.PaymentResponse::from)
                    .orElseThrow(() -> new IllegalStateException("Payment record not found"));
        }
        if (trade.getStatus() != P2PTradeStatus.PAYMENT_MARKED)
            throw new IllegalStateException("Trade is not awaiting payment verification");

        P2PPayment payment = paymentRepository.findByTradeId(tradeId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));
        if (payment.getStatus() == PaymentStatus.VERIFIED)
            return P2PPaymentDtos.PaymentResponse.from(payment);
        if (payment.getStatus() != PaymentStatus.SUBMITTED)
            throw new IllegalStateException("Payment is not awaiting verification");

        payment.setStatus(PaymentStatus.VERIFIED);
        payment.setVerifiedAt(LocalDateTime.now());
        return P2PPaymentDtos.PaymentResponse.from(paymentRepository.save(payment));
    }
}
