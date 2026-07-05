package com.proj.wallet.dtos;

import java.util.List;

import com.proj.wallet.model.Wallet;

public record GetWalletByUserIdResponse(List<Wallet> wallets, String message) {
    
}
