package com.proj.wallet.exceptions;

public class InsufficientBalanceException extends RuntimeException {

    public InsufficientBalanceException(String ex) {
        super(ex);
    }
}
