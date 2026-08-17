package com.proj.payment.service;

import java.util.UUID;

import com.proj.grpc.wallet.*;
import org.springframework.stereotype.Service;

import com.proj.events.TransactionEvent;
import com.proj.grpc.fraud.CheckTransferRequest;
import com.proj.grpc.fraud.CheckTransferResponse;
import com.proj.grpc.fraud.FraudServiceGrpc;
import com.proj.grpc.user.UserServiceGrpc;
import com.proj.payment.exception.FraudCheckException;
import com.proj.payment.kafka.TransactionEventPublisher;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.client.inject.GrpcClient;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final TransactionEventPublisher eventPublisher;

    @GrpcClient("user-service")
    private UserServiceGrpc.UserServiceBlockingStub userGrpcService;

    @GrpcClient("wallet-service")
    private WalletServiceGrpc.WalletServiceBlockingStub walletService;

    @GrpcClient("fraud-service")
    private FraudServiceGrpc.FraudServiceBlockingStub fraudService;

    public BalanceResponse topUp(CreditRequest request) {
        BalanceResponse response = walletService.credit(CreditRequest.newBuilder()
                .setAccountNo(request.getAccountNo())
                .setAmount(request.getAmount())
                .setCurrency(request.getCurrency())
                .build());

        eventPublisher.publish(new TransactionEvent(
                UUID.randomUUID().toString(),
                "TOPUP",
                null,
                request.getAccountNo(),
                request.getAmount(),
                request.getCurrency(),
                "COMPLETED",
                System.currentTimeMillis()));

        return response;
    }

    @Transactional
    public TransferResponse transfer(TransferRequest request) {

        CheckTransferResponse fraudCheck = fraudService.checkTransfer(CheckTransferRequest.newBuilder()
                .setFromAccount(request.getFromAccount())
                .setToAccount(request.getToAccount())
                .setAmount(request.getAmount())
                .setCurrency(request.getCurrency())
                .build());

        if (!fraudCheck.getAllowed()) {
            throw new FraudCheckException(fraudCheck.getReason());
        }

        TransferResponse response = walletService.transfer(TransferRequest.newBuilder()
                .setFromAccount(request.getFromAccount())
                .setToAccount(request.getToAccount())
                .setAmount(request.getAmount())
                .setCurrency(request.getCurrency())
                .build());

        eventPublisher.publish(new TransactionEvent(
                UUID.randomUUID().toString(),
                "TRANSFER",
                request.getFromAccount(),
                request.getToAccount(),
                request.getAmount(),
                request.getCurrency(),
                "COMPLETED",
                System.currentTimeMillis()));

        return response;
    }
}
