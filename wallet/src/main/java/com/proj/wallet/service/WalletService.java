package com.proj.wallet.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.proj.grpc.user.ExistsUserRequest;
import com.proj.grpc.user.UserServiceGrpc;
import com.proj.wallet.dtos.CreateWalletRequest;
import com.proj.wallet.model.Wallet;
import com.proj.wallet.repository.WalletRepository;

import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.client.inject.GrpcClient;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;

    @GrpcClient("user-service")
    private UserServiceGrpc.UserServiceBlockingStub userGrpcService;

    public Wallet createWallet(CreateWalletRequest request) {

        boolean userExists = userGrpcService.existsUser(
                ExistsUserRequest.newBuilder()
                        .setUserId(request.userId().toString())
                        .build())
                .getExists();

        if (!userExists) {
            throw new IllegalArgumentException("User with ID " + request.userId() + " does not exist.");
        }

        Wallet wallet = Wallet.builder()
                .userId(request.userId())
                .balance(BigDecimal.ZERO)
                .currency(request.currency())
                .status("PENDING")
                .updatedAt(LocalDateTime.now())
                .build();

        walletRepository.save(wallet);

        return wallet;
    }
    
    public Wallet getWalletById(UUID walletId) {
        return walletRepository.findById(walletId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet with ID " + walletId + " not found."));
    }

    public List<Wallet> getWalletsByUserId(UUID userId) {
        boolean userExists = userGrpcService.existsUser(
                ExistsUserRequest.newBuilder()
                        .setUserId(userId.toString())
                        .build())
                .getExists();
        if (!userExists) {
            throw new IllegalArgumentException("User with ID " + userId + " does not exist.");
        }
        return walletRepository.findByUserId(userId);
    }

    public void updateWallet(Wallet wallet) {
        if (!walletRepository.existsById(wallet.getWalletId())) {
            throw new IllegalArgumentException("Wallet with ID " + wallet.getWalletId() + " does not exist.");
        }

        walletRepository.save(wallet);
    }

    public void deleteWallet(UUID walletId) {
        if (!walletRepository.existsById(walletId)) {
            throw new IllegalArgumentException("Wallet with ID " + walletId + " does not exist.");
        }

        walletRepository.deleteById(walletId);
    }

    public boolean walletExists(UUID walletId) {
        return walletRepository.existsById(walletId);
    }

    public void changeWalletStatus(UUID walletId, String newStatus) {
        Wallet wallet = getWalletById(walletId);
        wallet.setStatus(newStatus);
        walletRepository.save(wallet);
    }
    
}
