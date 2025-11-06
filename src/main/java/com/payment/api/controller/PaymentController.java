package com.payment.api.controller;

import com.payment.api.dto.InitiatePaymentSessionRequest;
import com.payment.api.dto.InitiatePaymentSessionResponse;
import com.payment.api.service.InitiatePaymentSessionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final InitiatePaymentSessionService initiatePaymentSessionService;

    public PaymentController(InitiatePaymentSessionService initiatePaymentSessionService) {
        this.initiatePaymentSessionService = initiatePaymentSessionService;
    }

    @PostMapping("/initiate")
    public ResponseEntity<InitiatePaymentSessionResponse> initiatePaymentSession(
            @RequestBody InitiatePaymentSessionRequest request
    ) {
        var response = initiatePaymentSessionService.initiateSession(request);
        return ResponseEntity.ok(response);
    }
}
