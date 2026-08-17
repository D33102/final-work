package com.proj.ledger.service;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;

import com.proj.events.TransactionEvent;
import com.proj.ledger.model.LedgerEntry;
import com.proj.ledger.repository.LedgerRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class LedgerService {

    private final LedgerRepository ledgerRepository;

    public void record(TransactionEvent event) {
        if (ledgerRepository.existsByTransactionId(event.transactionId())) {
            log.info("Skipping duplicate transaction {}", event.transactionId());
            return;
        }

        LedgerEntry entry = LedgerEntry.builder()
                .transactionId(event.transactionId())
                .type(event.type())
                .fromAccount(event.fromAccount())
                .toAccount(event.toAccount())
                .amount(event.amount())
                .currency(event.currency())
                .status(event.status())
                .createdAt(Instant.ofEpochMilli(event.timestamp()))
                .build();

        ledgerRepository.save(entry);
        log.info("Recorded {} transaction {}", event.type(), event.transactionId());
    }

    public LedgerEntry getByTransactionId(String transactionId) {
        return ledgerRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No ledger entry for transaction " + transactionId));
    }

    public List<LedgerEntry> getByAccount(String accountNo) {
        return ledgerRepository.findByFromAccountOrToAccountOrderByCreatedAtDesc(accountNo, accountNo);
    }
}
