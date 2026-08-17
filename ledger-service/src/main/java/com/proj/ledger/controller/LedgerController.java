package com.proj.ledger.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proj.ledger.model.LedgerEntry;
import com.proj.ledger.service.LedgerService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/ledger")
@RequiredArgsConstructor
public class LedgerController {

    private final LedgerService ledgerService;

    @GetMapping("/transactions/{transactionId}")
    public ResponseEntity<LedgerEntry> getByTransactionId(@PathVariable String transactionId) {
        return ResponseEntity.ok(ledgerService.getByTransactionId(transactionId));
    }

    @GetMapping("/accounts/{accountNo}/transactions")
    public ResponseEntity<List<LedgerEntry>> getByAccount(@PathVariable String accountNo) {
        return ResponseEntity.ok(ledgerService.getByAccount(accountNo));
    }
}
