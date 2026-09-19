package com.oakpay.wallet.custody;

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

    public CustodyReconciliationService(CustodyOperationRepository operationRepository,
                                         CustodyProviderService providerService,
                                         CustodyWithdrawalService withdrawalService) {
        this.operationRepository = operationRepository;
        this.providerService = providerService;
        this.withdrawalService = withdrawalService;
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

        CustodyProvider.ProviderTransactionStatus providerStatus =
                providerService.getTransactionStatus(operation.getProviderName(), operation.getProviderReference());

        return withdrawalService.applyProviderStatus(
                operation.getProviderName(),
                operation.getProviderReference(),
                providerStatus);
    }
}
