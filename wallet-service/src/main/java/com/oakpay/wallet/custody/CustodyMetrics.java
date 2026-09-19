package com.oakpay.wallet.custody;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class CustodyMetrics {
    private final Counter submissionUnknown;
    private final Counter completed;
    private final Counter failed;
    private final Counter webhookProcessed;
    private final Counter reconciliationCompleted;
    private final Counter reconciliationFailed;

    public CustodyMetrics(MeterRegistry registry) {
        submissionUnknown = Counter.builder("oakpay.custody.submission.unknown")
                .description("Custody submissions whose outcome could not be determined")
                .register(registry);
        completed = Counter.builder("oakpay.custody.withdrawals.completed")
                .description("Custody withdrawals completed")
                .register(registry);
        failed = Counter.builder("oakpay.custody.withdrawals.failed")
                .description("Custody withdrawals failed")
                .register(registry);
        webhookProcessed = Counter.builder("oakpay.custody.webhooks.processed")
                .description("Custody webhooks successfully processed")
                .register(registry);
        reconciliationCompleted = Counter.builder("oakpay.custody.reconciliation.completed")
                .description("Custody reconciliation operations completed")
                .register(registry);
        reconciliationFailed = Counter.builder("oakpay.custody.reconciliation.failed")
                .description("Custody reconciliation operations failed")
                .register(registry);
    }

    public void submissionUnknown() { submissionUnknown.increment(); }
    public void completed() { completed.increment(); }
    public void failed() { failed.increment(); }
    public void webhookProcessed() { webhookProcessed.increment(); }
    public void reconciliationCompleted() { reconciliationCompleted.increment(); }
    public void reconciliationFailed() { reconciliationFailed.increment(); }
}
