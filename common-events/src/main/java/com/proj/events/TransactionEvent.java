package com.proj.events;

public record TransactionEvent(
        String transactionId,
        String type,
        String fromAccount,
        String toAccount,
        long amount,
        String currency,
        String status,
        long timestamp
) {}
