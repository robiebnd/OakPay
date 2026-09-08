package com.oakpay.wallet.deposit;

import com.oakpay.wallet.ledger.LedgerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class DepositService {
    private final DepositRepository depositRepository;
    private final DepositAddressRepository addressRepository;
    private final LedgerService ledgerService;

    public DepositService(DepositRepository depositRepository,
                           DepositAddressRepository addressRepository,
                           LedgerService ledgerService) {
        this.depositRepository = depositRepository;
        this.addressRepository = addressRepository;
        this.ledgerService = ledgerService;
    }

    @Transactional
    public DepositDtos.DepositResponse processWebhook(DepositDtos.BlockchainDepositWebhook request) {
        String network = normalize(request.network());
        String address = request.address().trim();
        String txHash = request.txHash().trim();
        String currency = normalize(request.currency());
        BigDecimal amount = request.amount();

        DepositAddress assigned = addressRepository
                .findByAddressAndNetworkAndStatus(address, network, DepositAddressStatus.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("Deposit address is not assigned to an active OakPay wallet"));

        if (!assigned.getCurrency().equals(currency)) {
            throw new IllegalArgumentException("Deposit currency does not match the assigned address");
        }
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Deposit amount must be greater than zero");
        }

        Deposit deposit = depositRepository.findWithLockByTxHashAndNetwork(txHash, network).orElse(null);
        if (deposit == null) {
            deposit = new Deposit();
            deposit.setUserId(assigned.getUserId());
            deposit.setDepositAddressId(assigned.getId());
            deposit.setCurrency(currency);
            deposit.setNetwork(network);
            deposit.setAddress(address);
            deposit.setTxHash(txHash);
            deposit.setAmount(amount);
            deposit.setConfirmations(request.confirmations());
            deposit.setRequiredConfirmations(request.requiredConfirmations());
            deposit.setStatus(statusFor(request.confirmations(), request.requiredConfirmations()));
            deposit = depositRepository.saveAndFlush(deposit);
        } else {
            if (!deposit.getUserId().equals(assigned.getUserId())
                    || !deposit.getDepositAddressId().equals(assigned.getId())
                    || !deposit.getCurrency().equals(currency)
                    || deposit.getAmount().compareTo(amount) != 0) {
                throw new IllegalArgumentException("Blockchain transaction conflicts with the original deposit record");
            }
            if (deposit.getStatus() == DepositStatus.COMPLETED) return DepositDtos.DepositResponse.from(deposit);
            deposit.setConfirmations(Math.max(deposit.getConfirmations(), request.confirmations()));
            deposit.setRequiredConfirmations(Math.max(deposit.getRequiredConfirmations(), request.requiredConfirmations()));
            deposit.setStatus(statusFor(deposit.getConfirmations(), deposit.getRequiredConfirmations()));
        }

        if (deposit.getStatus() == DepositStatus.COMPLETED) {
            String reference = "CHAIN-DEPOSIT:" + deposit.getNetwork() + ":" + deposit.getTxHash();
            ledgerService.creditExternalDeposit(deposit.getUserId(), deposit.getCurrency(), deposit.getAmount(),
                    reference, "onchain tx=" + deposit.getTxHash() + ";network=" + deposit.getNetwork());
        }

        return DepositDtos.DepositResponse.from(depositRepository.save(deposit));
    }

    @Transactional(readOnly = true)
    public List<DepositDtos.DepositResponse> getUserDeposits(UUID userId) {
        return depositRepository.findTop50ByUserIdOrderByDetectedAtDesc(userId).stream()
                .map(DepositDtos.DepositResponse::from).toList();
    }

    private DepositStatus statusFor(int confirmations, int required) {
        return confirmations >= required ? DepositStatus.COMPLETED
                : confirmations > 0 ? DepositStatus.CONFIRMING : DepositStatus.PENDING;
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Value is required");
        return value.trim().toUpperCase();
    }
}
