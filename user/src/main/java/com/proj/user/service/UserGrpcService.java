package com.proj.user.service;

import java.util.UUID;

import com.proj.grpc.user.ExistsUserRequest;
import com.proj.grpc.user.ExistsUserResponse;
import com.proj.grpc.user.UserServiceGrpc;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
@RequiredArgsConstructor
public class UserGrpcService extends UserServiceGrpc.UserServiceImplBase {

    private final UserService userService;

    @Override
    public void existsUser(ExistsUserRequest request,
                      StreamObserver<ExistsUserResponse> responseObserver) {
        boolean exists = userService.userExistsById(UUID.fromString(request.getUserId()));
        ExistsUserResponse response = ExistsUserResponse.newBuilder()
                .setExists(exists)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
    
}
