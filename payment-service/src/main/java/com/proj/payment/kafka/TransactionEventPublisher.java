package com.proj.payment.kafka;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.proj.events.TransactionEvent;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TransactionEventPublisher {

    private final KafkaTemplate<String, TransactionEvent> kafkaTemplate;

    @Value("${ledger.topic}")
    private String topic;

    public void publish(TransactionEvent event) {
        kafkaTemplate.send(topic, event.transactionId(), event);
    }
}
