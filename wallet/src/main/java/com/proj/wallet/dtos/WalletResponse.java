package com.proj.wallet.dtos;

import com.proj.wallet.model.Wallet;

public record WalletResponse(Wallet wallet, String message) {

}
