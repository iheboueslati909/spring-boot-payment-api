package com.payment.api.controller;

import com.payment.api.dto.StripeWebhookResponse;
import com.payment.api.service.StripeWebhookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/stripe/webhook")
@Tag(name = "Stripe Webhook", description = "Stripe webhook endpoints for payment event handling")
public class StripeWebhookController {

    private final StripeWebhookService stripeWebhookService;

    public StripeWebhookController(StripeWebhookService stripeWebhookService) {
        this.stripeWebhookService = stripeWebhookService;
    }

    @PostMapping
    @Operation(summary = "Handle Stripe webhook events", description = "Handles various Stripe webhook events including payment success and failure notifications")
    public ResponseEntity<StripeWebhookResponse> handleWebhook(HttpServletRequest request,
            @RequestBody String payload) throws IOException {
        StripeWebhookResponse response = stripeWebhookService.handleWebhook(payload,
                request.getHeader("Stripe-Signature"));

        if ("FAILED".equals(response.paymentStatus())) {
            return ResponseEntity.badRequest().body(response);
        }

        return ResponseEntity.ok(response);
    }
}