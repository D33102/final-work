package com.proj.wallet.dtos;

import java.util.UUID;

public record CreateWalletRequest(UUID userId, String currency, String status, String updatedAt) {

}
