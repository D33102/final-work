package com.proj.user.service;

import java.util.UUID;

import com.proj.grpc.user.CreateUserRequest;
import com.proj.grpc.user.CreateUserResponse;
import com.proj.grpc.user.ExistsUserRequest;
import com.proj.grpc.user.ExistsUserResponse;
import com.proj.grpc.user.GetUserRequest;
import com.proj.grpc.user.UserResponse;
import com.proj.grpc.user.UserServiceGrpc;
import com.proj.user.model.User;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
@RequiredArgsConstructor
public class UserGrpcService extends UserServiceGrpc.UserServiceImplBase {

    private final UserService userService;

    @Override
    public void createUser(CreateUserRequest request,
                      StreamObserver<CreateUserResponse> responseObserver) {
        try {
            User user = userService.registerProfile(
                    request.getName(),
                    request.getEmail(),
                    request.getPhoneNumber());

            CreateUserResponse response = CreateUserResponse.newBuilder()
                    .setUserId(user.getUserId().toString())
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        }
    }

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

    @Override
    public void getUser(GetUserRequest request,
                      StreamObserver<UserResponse> responseObserver) {
        try {
            User user = userService.getUserById(UUID.fromString(request.getUserId()));
            UserResponse response = UserResponse.newBuilder()
                    .setUserId(user.getUserId().toString())
                    .setEmail(user.getEmail())
                    .setFullName(user.getName())
                    .setPhoneNumber(user.getPhoneNumber())
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        }
    }

}
