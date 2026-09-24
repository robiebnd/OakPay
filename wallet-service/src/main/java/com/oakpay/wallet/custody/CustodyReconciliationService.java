package com.oakpay.wallet.custody;

import com.oakpay.wallet.deposit.Deposit;
import com.oakpay.wallet.deposit.DepositDtos;
import com.oakpay.wallet.deposit.DepositRepository;
import com.oakpay.wallet.deposit.DepositAddressRepository;
import com.oakpay.wallet.deposit.DepositAddressStatus;
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
    private final DepositAddressRepository depositAddressRepository;
    private final DepositService depositService;
    private final String configuredProvider;

    public CustodyReconciliationService(CustodyOperationRepository operationRepository,
                                         CustodyProviderService providerService,
                                         CustodyWithdrawalService withdrawalService,
                                         CustodyMetrics metrics,
                                         DepositRepository depositRepository,
                                         DepositAddressRepository depositAddressRepository,
                                         DepositService depositService,
                                         @Value("${oakpay.custody.provider-name:TATUM}") String configuredProvider) {
        this.operationRepository = operationRepository;
        this.providerService = providerService;
        this.withdrawalService = withdrawalService;
        this.metrics = metrics;
        this.depositRepository = depositRepository;
        this.depositAddressRepository = depositAddressRepository;
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
    public List<DepositDtos.DepositResponse> discoverDeposits(String currency, String network, String address) {
        String normalizedCurrency = currency == null ? "" : currency.trim().toUpperCase();
        String normalizedNetwork = network == null ? "" : network.trim().toUpperCase();
        var assigned = depositAddressRepository
                .findByAddressAndNetworkAndStatus(address, normalizedNetwork, DepositAddressStatus.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("Deposit address is not assigned to an active OakPay wallet"));
        if (!assigned.getCurrency().equals(normalizedCurrency)) {
            throw new IllegalArgumentException("Deposit currency does not match the assigned address");
        }

        List<DepositDtos.DepositResponse> results = new java.util.ArrayList<>();
        var discovered = providerService.findIncomingDeposits(
                configuredProvider, normalizedCurrency, normalizedNetwork, assigned.getAddress());
        for (var candidate : discovered) {
            var status = providerService.getDepositTransactionStatus(
                    configuredProvider, normalizedCurrency, normalizedNetwork, candidate.transactionHash());
            var request = new DepositDtos.BlockchainDepositWebhook(
                    normalizedNetwork,
                    assigned.getAddress(),
                    candidate.transactionHash(),
                    normalizedCurrency,
                    candidate.amount(),
                    status.confirmations(),
                    candidate.requiredConfirmations());
            results.add(depositService.processWebhook(request));
        }
        return results;
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
