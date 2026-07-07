package com.proj.wallet.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import com.proj.wallet.exceptions.InsufficientBalanceException;
import com.proj.wallet.exceptions.WalletNotFoundException;
import jakarta.transaction.Transactional;
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
                .accountNo(generateAccountNumber())
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

    @Transactional
    public void transfer(String fromAcc, String toAcc, BigDecimal amount, String currency) throws WalletNotFoundException, InsufficientBalanceException {

        Wallet sender = walletRepository.findByAccountNoForUpdate(fromAcc)
                .orElseThrow(() -> new WalletNotFoundException("Sender wallet not found"));

        Wallet receiver = walletRepository.findByAccountNoForUpdate(toAcc)
                .orElseThrow(() -> new WalletNotFoundException("Receiver wallet not found"));

        if (sender.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Insufficient balance");
        }

        sender.setBalance(sender.getBalance().subtract(amount));
        receiver.setBalance(receiver.getBalance().add(amount));

        sender.setUpdatedAt(LocalDateTime.now());
        receiver.setUpdatedAt(LocalDateTime.now());

        walletRepository.save(sender);
        walletRepository.save(receiver);
    }

    @Transactional
    public void credit(String accountNo, BigDecimal amount) throws WalletNotFoundException {

        Wallet wallet = walletRepository.findByAccountNoForUpdate(accountNo)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found"));

        wallet.setBalance(wallet.getBalance().add(amount));
        wallet.setUpdatedAt(LocalDateTime.now());

        walletRepository.save(wallet);
    }

    public int getBalance(String account) {
        UUID walletId = walletRepository.findByAccountNoForUpdate(account).orElseThrow(() -> new WalletNotFoundException("Sender wallet not found")).getWalletId();
        BigDecimal balance = walletRepository.findById(walletId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet with ID " + walletId + " not found.")).getBalance();
        return balance.multiply(new BigDecimal("100")).intValue();
    }

    public boolean walletExists(UUID walletId) {
        return walletRepository.existsById(walletId);
    }

    public void changeWalletStatus(UUID walletId, String newStatus) {
        Wallet wallet = getWalletById(walletId);
        wallet.setStatus(newStatus);
        walletRepository.save(wallet);
    }

    private String generateAccountNumber() {
        long random = ThreadLocalRandom.current().nextLong(1000000L, 10000000L);
        return "134" + random;
    }
    
}
