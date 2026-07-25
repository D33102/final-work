package com.proj.payment.service;

import java.math.BigDecimal;

import com.proj.grpc.wallet.*;
import org.springframework.stereotype.Service;

import com.proj.grpc.user.UserServiceGrpc;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.client.inject.GrpcClient;

@Service
@RequiredArgsConstructor
public class PaymentService {

    @GrpcClient("user-service")
    private UserServiceGrpc.UserServiceBlockingStub userGrpcService;

    @GrpcClient("wallet-service")
    private WalletServiceGrpc.WalletServiceBlockingStub walletService;

    public BalanceResponse topUp(CreditRequest request) {
        return walletService.credit(CreditRequest.newBuilder()
                .setAccountNo(request.getAccountNo())
                .setAmount(request.getAmount())
                .setCurrency(request.getCurrency())
                .build());
        
    }
    
    @Transactional
    public TransferResponse transfer(TransferRequest request) {
        return walletService.transfer(TransferRequest.newBuilder()
                .setFromAccount(request.getFromAccount())
                .setToAccount(request.getToAccount())
                .setAmount(request.getAmount())
                .setCurrency(request.getCurrency())
                .build());
    }
}
