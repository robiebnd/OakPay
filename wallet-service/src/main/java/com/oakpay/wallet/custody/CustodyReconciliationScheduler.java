package com.oakpay.wallet.custody;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class CustodyReconciliationScheduler {
    private static final Logger log = LoggerFactory.getLogger(CustodyReconciliationScheduler.class);

    private final CustodyReconciliationService reconciliationService;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final boolean enabled;
    private final int olderThanMinutes;

    public CustodyReconciliationScheduler(
            CustodyReconciliationService reconciliationService,
            @Value("${oakpay.custody.reconciliation.enabled:true}") boolean enabled,
            @Value("${oakpay.custody.reconciliation.older-than-minutes:5}") int olderThanMinutes) {
        this.reconciliationService = reconciliationService;
        this.enabled = enabled;
        this.olderThanMinutes = Math.max(1, Math.min(1440, olderThanMinutes));
    }

    @Scheduled(
            fixedDelayString = "${oakpay.custody.reconciliation.fixed-delay-ms:60000}",
            initialDelayString = "${oakpay.custody.reconciliation.initial-delay-ms:30000}")
    public void reconcileStaleWithdrawals() {
        if (!enabled || !running.compareAndSet(false, true)) {
            return;
        }

        try {
            LocalDateTime cutoff = LocalDateTime.now().minusMinutes(olderThanMinutes);
            var pending = reconciliationService.findPendingWithdrawals(cutoff);

            int attempted = 0;
            int skipped = 0;
            for (CustodyOperation operation : pending) {
                if (operation.getProviderReference() == null
                        || operation.getProviderReference().isBlank()) {
                    skipped++;
                    continue;
                }

                attempted++;
                try {
                    reconciliationService.reconcileWithdrawal(operation.getId());
                } catch (RuntimeException e) {
                    log.warn("Custody reconciliation failed for operation {}", operation.getOperationId(), e);
                }
            }

            if (!pending.isEmpty()) {
                log.info("Custody reconciliation cycle completed: pending={}, attempted={}, skipped={}",
                        pending.size(), attempted, skipped);
            }
        } finally {
            running.set(false);
        }
    }
}
