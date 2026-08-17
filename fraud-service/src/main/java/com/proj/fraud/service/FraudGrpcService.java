package com.proj.fraud.service;

import com.proj.grpc.fraud.CheckTransferRequest;
import com.proj.grpc.fraud.CheckTransferResponse;
import com.proj.grpc.fraud.FraudServiceGrpc;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
@RequiredArgsConstructor
public class FraudGrpcService extends FraudServiceGrpc.FraudServiceImplBase {

    private final FraudService fraudService;

    @Override
    public void checkTransfer(CheckTransferRequest request,
                      StreamObserver<CheckTransferResponse> responseObserver) {

        FraudService.Decision decision = fraudService.check(
                request.getFromAccount(),
                request.getToAccount(),
                request.getAmount(),
                request.getCurrency());

        CheckTransferResponse response = CheckTransferResponse.newBuilder()
                .setAllowed(decision.allowed())
                .setReason(decision.reason())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
