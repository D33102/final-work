package com.proj.wallet.service;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.proj.wallet.dtos.CreateWalletRequest;
import com.proj.wallet.model.Wallet;
import com.proj.wallet.repository.WalletRepository;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @InjectMocks
    private WalletService walletService;

    @Test
    void shouldCreateWalletSuccessfully() {

        CreateWalletRequest request =
                new CreateWalletRequest(UUID.randomUUID(), "USD", null, null);

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
