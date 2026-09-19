package com.oakpay.wallet.custody;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("custodyProvider")
public class CustodyHealthIndicator implements HealthIndicator {
    private final ObjectProvider<CustodyProvider> providerProvider;
    private final CustodyOperationRepository operationRepository;

    public CustodyHealthIndicator(ObjectProvider<CustodyProvider> providerProvider,
                                  CustodyOperationRepository operationRepository) {
        this.providerProvider = providerProvider;
        this.operationRepository = operationRepository;
    }

    @Override
    public Health health() {
        CustodyProvider provider = providerProvider.getIfAvailable();
        if (provider == null) {
            return Health.unknown()
                    .withDetail("reason", "No custody provider configured")
                    .build();
        }

        long recoverable = operationRepository.countByOperationTypeAndStatusIn(
                CustodyOperationType.WITHDRAWAL,
                List.of(
                        CustodyOperationStatus.SUBMISSION_UNKNOWN,
                        CustodyOperationStatus.REQUESTED,
                        CustodyOperationStatus.SUBMITTED,
                        CustodyOperationStatus.PROCESSING));

        if (recoverable > 0) {
            return Health.up()
                    .withDetail("provider", provider.providerName())
                    .withDetail("recoveryQueue", recoverable)
                    .build();
        }

        return Health.up()
                .withDetail("provider", provider.providerName())
                .withDetail("recoveryQueue", 0)
                .build();
    }
}
