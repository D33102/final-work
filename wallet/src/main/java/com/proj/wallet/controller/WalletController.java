package com.proj.wallet.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proj.wallet.dtos.CreateWalletRequest;
import com.proj.wallet.dtos.GetWalletByUserIdResponse;
import com.proj.wallet.dtos.WalletResponse;
import com.proj.wallet.model.Wallet;
import com.proj.wallet.service.WalletService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/wallets")
@RequiredArgsConstructor
public class WalletController {
    
    private final WalletService walletService;

    @GetMapping
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Wallet service is running!");
    }

    @GetMapping("/{walletId}")
    public ResponseEntity<Wallet> getWalletById(@PathVariable UUID walletId) {
        Wallet wallet = walletService.getWalletById(walletId);
        return ResponseEntity.ok(wallet); 
    }

    @GetMapping("/{userId}")
    public ResponseEntity<GetWalletByUserIdResponse> getWalletsByUserId(@PathVariable UUID userId) {
        List<Wallet> wallets = walletService.getWalletsByUserId(userId);
        GetWalletByUserIdResponse response = new GetWalletByUserIdResponse(wallets, "Wallets retrieved successfully.");
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<WalletResponse> createWallet(
            @RequestBody CreateWalletRequest request) {
        
        Wallet wallet = walletService.createWallet(request);
        WalletResponse response = new WalletResponse(wallet, "Wallet created successfully.");
        return ResponseEntity.ok(response);
    }
}
