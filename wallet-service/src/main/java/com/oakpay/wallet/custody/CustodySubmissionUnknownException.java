package com.oakpay.wallet.custody;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.ACCEPTED)
public class CustodySubmissionUnknownException extends RuntimeException {
    public CustodySubmissionUnknownException(String message, Throwable cause) {
        super(message, cause);
    }
}
