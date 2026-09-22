package com.oakpay.wallet.custody;

import com.oakpay.wallet.deposit.Deposit;
import com.oakpay.wallet.deposit.DepositDtos;
import com.oakpay.wallet.deposit.DepositRepository;
import com.oakpay.wallet.deposit.DepositService;
import com.oakpay.wallet.deposit.DepositStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class CustodyReconciliationService {
    private final CustodyOperationRepository operationRepository;
    private final CustodyProviderService providerService;
    private final CustodyWithdrawalService withdrawalService;
    private final CustodyMetrics metrics;
    private final DepositRepository depositRepository;
    private final DepositService depositService;
    private final String configuredProvider;

    public CustodyReconciliationService(CustodyOperationRepository operationRepository,
                                         CustodyProviderService providerService,
                                         CustodyWithdrawalService withdrawalService,
                                         CustodyMetrics metrics,
                                         DepositRepository depositRepository,
                                         DepositService depositService,
                                         @Value("${oakpay.custody.provider-name:TATUM}") String configuredProvider) {
        this.operationRepository = operationRepository;
        this.providerService = providerService;
        this.withdrawalService = withdrawalService;
        this.metrics = metrics;
        this.depositRepository = depositRepository;
        this.depositService = depositService;
        this.configuredProvider = configuredProvider == null || configuredProvider.isBlank() ? "TATUM" : configuredProvider.trim();
    }

    @Transactional(readOnly = true)
    public List<CustodyOperation> findPendingWithdrawals(LocalDateTime cutoff) {
        return operationRepository.findTop100ByOperationTypeAndStatusInAndUpdatedAtBeforeOrderByUpdatedAtAsc(
                CustodyOperationType.WITHDRAWAL,
                List.of(
                        CustodyOperationStatus.SUBMISSION_UNKNOWN,
                        CustodyOperationStatus.REQUESTED,
                        CustodyOperationStatus.SUBMITTED,
                        CustodyOperationStatus.PROCESSING),
                cutoff);
    }

    @Transactional(readOnly = true)
    public List<Deposit> findPendingDeposits(LocalDateTime cutoff) {
        return depositRepository.findTop100PendingForReconciliation(
                List.of(DepositStatus.PENDING, DepositStatus.CONFIRMING),
                cutoff,
                PageRequest.of(0, 100));
    }

    @Transactional
    public DepositDtos.DepositResponse reconcileDeposit(UUID depositId) {
        Deposit deposit = depositRepository.findById(depositId)
                .orElseThrow(() -> new IllegalArgumentException("Deposit not found"));
        CustodyProviderService.DepositStatusResult providerStatus = providerService.getDepositTransactionStatus(
                configuredProvider, deposit.getCurrency(), deposit.getNetwork(), deposit.getTxHash());
        DepositDtos.DepositResponse result = depositService.reconcileDeposit(depositId, providerStatus.confirmations());
        if ("COMPLETED".equalsIgnoreCase(result.status())) metrics.reconciliationCompleted();
        return result;
    }

    @Transactional
    public CustodyOperation reconcileWithdrawal(UUID operationId) {
        CustodyOperation operation = operationRepository.findById(operationId)
                .orElseThrow(() -> new IllegalArgumentException("Custody operation not found"));

        if (operation.getOperationType() != CustodyOperationType.WITHDRAWAL) {
            throw new IllegalArgumentException("Custody operation is not a withdrawal");
        }
        if (operation.getProviderReference() == null || operation.getProviderReference().isBlank()) {
            throw new IllegalStateException("Withdrawal has no provider reference");
        }

        if (operation.getStatus() == CustodyOperationStatus.COMPLETED
                || operation.getStatus() == CustodyOperationStatus.FAILED) {
            return operation;
        }

        try {
            CustodyProvider.ProviderTransactionStatus providerStatus =
                    providerService.getTransactionStatus(operation.getProviderName(), operation.getProviderReference());

            CustodyOperation reconciled = withdrawalService.applyProviderStatus(
                    operation.getProviderName(),
                    operation.getProviderReference(),
                    providerStatus);
            if (reconciled.getStatus() == CustodyOperationStatus.COMPLETED) {
                metrics.reconciliationCompleted();
            } else if (reconciled.getStatus() == CustodyOperationStatus.FAILED) {
                metrics.reconciliationFailed();
            }
            return reconciled;
        } catch (RuntimeException e) {
            metrics.reconciliationFailed();
            throw e;
        }
    }
}
