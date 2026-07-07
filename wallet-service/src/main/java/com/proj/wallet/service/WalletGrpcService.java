package com.proj.wallet.service;

import java.math.BigDecimal;

import com.proj.grpc.wallet.TransferRequest;
import com.proj.grpc.wallet.TransferResponse;
import com.proj.grpc.wallet.WalletServiceGrpc;
import com.proj.wallet.exceptions.InsufficientBalanceException;
import com.proj.wallet.exceptions.WalletNotFoundException;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
@RequiredArgsConstructor
public class WalletGrpcService extends WalletServiceGrpc.WalletServiceImplBase {

    private final WalletService walletService;

    @Override
    public void transfer(TransferRequest request,
                        StreamObserver<TransferResponse> responseObserver) {

        try {
            BigDecimal amount = BigDecimal.valueOf(request.getAmount(), 2);

            walletService.transfer(request.getFromAccount(), request.getToAccount(),
                    amount, request.getCurrency());

            TransferResponse response = TransferResponse.newBuilder()
                    .setSenderBalance(walletService.getBalance(request.getFromAccount()))
                    .setReceiverBalance(walletService.getBalance(request.getToAccount()))
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (WalletNotFoundException e) {
            responseObserver.onError(Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
        } catch (InsufficientBalanceException e) {
            responseObserver.onError(Status.FAILED_PRECONDITION.withDescription(e.getMessage()).asRuntimeException());
        }
    }
}
