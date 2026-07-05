package com.proj.wallet.service;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.proj.grpc.user.ExistsUserResponse;
import com.proj.grpc.user.UserServiceGrpc;
import com.proj.wallet.dtos.CreateWalletRequest;
import com.proj.wallet.model.Wallet;
import com.proj.wallet.repository.WalletRepository;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private UserServiceGrpc.UserServiceBlockingStub userGrpcService;

    @InjectMocks
    private WalletService walletService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(walletService, "userGrpcService", userGrpcService);
    }

    @Test
    void shouldCreateWalletSuccessfully() {

        CreateWalletRequest request =
                new CreateWalletRequest(UUID.randomUUID(), "USD");

        when(userGrpcService.existsUser(any()))
                .thenReturn(ExistsUserResponse.newBuilder().setExists(true).build());

        Wallet savedWallet = Wallet.builder()
                .walletId(UUID.randomUUID())
                .userId(request.userId())
                .balance(BigDecimal.ZERO)
                .currency("USD")
                .build();

        when(walletRepository.save(any(Wallet.class)))
                .thenReturn(savedWallet);

        Wallet result = walletService.createWallet(request);

        assertEquals(request.userId(), result.getUserId());
        assertEquals(BigDecimal.ZERO, result.getBalance());

        verify(walletRepository, times(1)).save(any(Wallet.class));
    }
}
