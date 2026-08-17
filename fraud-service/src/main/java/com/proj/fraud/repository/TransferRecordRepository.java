package com.proj.fraud.repository;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.proj.fraud.model.TransferRecord;

public interface TransferRecordRepository extends JpaRepository<TransferRecord, UUID> {

    long countByFromAccountAndCreatedAtAfter(String fromAccount, Instant after);

    @Query("select coalesce(sum(t.amount), 0) from TransferRecord t "
            + "where t.fromAccount = ?1 and t.createdAt >= ?2")
    long sumAmountByFromAccountSince(String fromAccount, Instant since);
}
