package com.proj.wallet.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.proj.wallet.model.Wallet;

public interface WalletRepository extends JpaRepository<Wallet, UUID> {
    
    List<Wallet> findByUserId(UUID userId);
}
