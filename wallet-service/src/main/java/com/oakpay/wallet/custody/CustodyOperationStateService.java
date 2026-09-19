package com.oakpay.wallet.custody;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustodyOperationStateService {
    private final CustodyOperationRepository operationRepository;

    public CustodyOperationStateService(CustodyOperationRepository operationRepository) {
        this.operationRepository = operationRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markSubmissionUnknown(CustodyOperation operation) {
        CustodyOperation current = operationRepository.findById(operation.getId())
                .orElseThrow(() -> new IllegalStateException("Custody operation disappeared during recovery"));
        current.setStatus(CustodyOperationStatus.SUBMISSION_UNKNOWN);
        operationRepository.saveAndFlush(current);
    }
}
