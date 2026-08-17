package com.proj.ledger.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.proj.events.TransactionEvent;
import com.proj.ledger.service.LedgerService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TransactionEventConsumer {

    private final LedgerService ledgerService;

    @KafkaListener(topics = "${ledger.topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void onTransaction(TransactionEvent event) {
        ledgerService.record(event);
    }
}
