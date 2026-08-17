package com.proj.ledger.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.proj.ledger.model.LedgerEntry;

public interface LedgerRepository extends JpaRepository<LedgerEntry, UUID> {

    boolean existsByTransactionId(String transactionId);

    Optional<LedgerEntry> findByTransactionId(String transactionId);

    List<LedgerEntry> findByFromAccountOrToAccountOrderByCreatedAtDesc(String fromAccount, String toAccount);
}
