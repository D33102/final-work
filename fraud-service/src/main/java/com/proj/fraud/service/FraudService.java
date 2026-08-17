package com.proj.fraud.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Service;

import com.proj.fraud.config.FraudProperties;
import com.proj.fraud.model.TransferRecord;
import com.proj.fraud.repository.TransferRecordRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FraudService {

    private final FraudProperties properties;
    private final TransferRecordRepository transferRecordRepository;

    public record Decision(boolean allowed, String reason) {
        static Decision allow() {
            return new Decision(true, "OK");
        }

        static Decision deny(String reason) {
            return new Decision(false, reason);
        }
    }

    public Decision check(String fromAccount, String toAccount, long amount, String currency) {

        if (amount > properties.getMaxAmount()) {
            return Decision.deny("Amount " + amount + " exceeds max allowed " + properties.getMaxAmount());
        }

        if (properties.getBlocklist().contains(fromAccount) || properties.getBlocklist().contains(toAccount)) {
            return Decision.deny("Account is blocklisted");
        }

        Instant now = Instant.now();

        Instant windowStart = now.minusSeconds(properties.getVelocity().getWindowSeconds());
        long recentCount = transferRecordRepository.countByFromAccountAndCreatedAtAfter(fromAccount, windowStart);
        if (recentCount >= properties.getVelocity().getMaxCount()) {
            return Decision.deny("Velocity limit exceeded: " + recentCount + " transfers in the last "
                    + properties.getVelocity().getWindowSeconds() + "s");
        }

        Instant startOfDay = now.truncatedTo(ChronoUnit.DAYS);
        long spentToday = transferRecordRepository.sumAmountByFromAccountSince(fromAccount, startOfDay);
        if (spentToday + amount > properties.getDailyLimit()) {
            return Decision.deny("Daily limit exceeded: " + (spentToday + amount) + " > " + properties.getDailyLimit());
        }

        transferRecordRepository.save(TransferRecord.builder()
                .fromAccount(fromAccount)
                .toAccount(toAccount)
                .amount(amount)
                .currency(currency)
                .createdAt(now)
                .build());

        return Decision.allow();
    }
}
