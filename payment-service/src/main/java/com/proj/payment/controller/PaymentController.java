package com.proj.payment.controller;


import com.proj.grpc.wallet.BalanceResponse;
import com.proj.grpc.wallet.CreditRequest;
import com.proj.grpc.wallet.TransferRequest;
import com.proj.grpc.wallet.TransferResponse;
import com.proj.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/topup")
    public ResponseEntity<BalanceResponse> topUp(@RequestBody CreditRequest request) {
        return ResponseEntity.ok(paymentService.topUp(request));
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransferResponse> transfer(@RequestBody TransferRequest request) {
        return ResponseEntity.ok(paymentService.transfer(request));
    }

}
