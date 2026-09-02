package com.oakpay.trading.p2p;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class P2PPaymentAccountService {
    private static final List<String> METHODS = List.of("ECOCASH", "INNBUCKS", "ZIPIT", "BANK_TRANSFER", "MOBILE_MONEY", "CASH_DEPOSIT");
    private final P2PPaymentAccountRepository repository;

    public P2PPaymentAccountService(P2PPaymentAccountRepository repository) { this.repository = repository; }

    @Transactional
    public P2PPaymentAccountDtos.Response create(UUID ownerId, P2PPaymentAccountDtos.CreateRequest request) {
        if (request == null || request.method() == null || request.method().isBlank()) throw new IllegalArgumentException("Payment method is required");
        if (request.accountName() == null || request.accountName().isBlank()) throw new IllegalArgumentException("Account name is required");
        if (request.accountIdentifier() == null || request.accountIdentifier().isBlank()) throw new IllegalArgumentException("Account identifier is required");
        String method = request.method().trim().toUpperCase();
        if (!METHODS.contains(method)) throw new IllegalArgumentException("Unsupported payment method");

        P2PPaymentAccount account = new P2PPaymentAccount();
        account.setOwnerId(ownerId);
        account.setMethod(method);
        account.setAccountName(request.accountName().trim());
        account.setAccountIdentifier(request.accountIdentifier().trim());
        account.setInstructions(request.instructions());
        account.setActive(true);
        return P2PPaymentAccountDtos.Response.from(repository.save(account));
    }

    @Transactional(readOnly = true)
    public List<P2PPaymentAccountDtos.Response> mine(UUID ownerId) {
        return repository.findAllByOwnerIdAndActiveTrueOrderByCreatedAtDesc(ownerId).stream()
                .map(P2PPaymentAccountDtos.Response::from).toList();
    }

    @Transactional
    public void deactivate(UUID ownerId, UUID accountId) {
        P2PPaymentAccount account = repository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Payment account not found"));
        if (!account.getOwnerId().equals(ownerId)) throw new IllegalArgumentException("Payment account does not belong to user");
        account.setActive(false);
        repository.save(account);
    }
}
