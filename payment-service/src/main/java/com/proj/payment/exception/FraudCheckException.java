package com.proj.payment.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class FraudCheckException extends RuntimeException {

    public FraudCheckException(String reason) {
        super(reason);
    }
}
