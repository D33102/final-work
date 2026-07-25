package com.proj.auth.service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.proj.auth.dto.AuthResponse;
import com.proj.auth.dto.LoginRequest;
import com.proj.auth.dto.RefreshRequest;
import com.proj.auth.dto.RegisterRequest;
import com.proj.auth.model.AuthUser;
import com.proj.auth.repository.AuthUserRepository;
import com.proj.grpc.user.CreateUserRequest;
import com.proj.grpc.user.CreateUserResponse;
import com.proj.grpc.user.GetUserRequest;
import com.proj.grpc.user.UserResponse;
import com.proj.grpc.user.UserServiceGrpc;

import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.client.inject.GrpcClient;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthUserRepository authUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @GrpcClient("user-service")
    private UserServiceGrpc.UserServiceBlockingStub userGrpcClient;

    public AuthResponse register(RegisterRequest request) {

        CreateUserResponse created = userGrpcClient.createUser(
                CreateUserRequest.newBuilder()
                        .setName(request.name())
                        .setEmail(request.email())
                        .setPhoneNumber(request.phoneNumber())
                        .build());

        UUID userId = UUID.fromString(created.getUserId());

        Map<String, Object> claims = new HashMap<>();
        claims.put("email", request.email());
        claims.put("name", request.name());

        String accessToken = jwtService.generateToken(userId.toString(), claims);
        String refreshToken = jwtService.generateRefreshToken(userId.toString());

        AuthUser credential = AuthUser.builder()
                .userId(userId)
                .passwordHash(passwordEncoder.encode(request.password()))
                .refreshToken(refreshToken)
                .build();
        authUserRepository.save(credential);

        return new AuthResponse(userId.toString(), accessToken, refreshToken);
    }

    public AuthResponse login(LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.userId(), request.password()));

        AuthUser credential = authUserRepository.findById(UUID.fromString(request.userId()))
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        UserResponse user = userGrpcClient.getUser(
                GetUserRequest.newBuilder()
                        .setUserId(request.userId())
                        .build());

        Map<String, Object> claims = new HashMap<>();
        claims.put("email", user.getEmail());
        claims.put("name", user.getFullName());

        String accessToken = jwtService.generateToken(request.userId(), claims);
        String refreshToken = jwtService.generateRefreshToken(request.userId());

        credential.setRefreshToken(refreshToken);
        authUserRepository.save(credential);

        return new AuthResponse(request.userId(), accessToken, refreshToken);
    }

    public AuthResponse refresh(RefreshRequest request) {

        String presented = request.refreshToken();

        String userId;
        try {
            userId = jwtService.extractUserId(presented);
        } catch (JwtException e) {
            throw new BadCredentialsException("Invalid or expired refresh token");
        }

        AuthUser credential = authUserRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));

        if (credential.getRefreshToken() == null
                || !credential.getRefreshToken().equals(presented)) {
            throw new BadCredentialsException("Refresh token has been revoked");
        }

        UserResponse user = userGrpcClient.getUser(
                GetUserRequest.newBuilder()
                        .setUserId(userId)
                        .build());

        Map<String, Object> claims = new HashMap<>();
        claims.put("email", user.getEmail());
        claims.put("name", user.getFullName());

        String accessToken = jwtService.generateToken(userId, claims);
        String rotatedRefreshToken = jwtService.generateRefreshToken(userId);

        credential.setRefreshToken(rotatedRefreshToken);
        authUserRepository.save(credential);

        return new AuthResponse(userId, accessToken, rotatedRefreshToken);
    }
}
