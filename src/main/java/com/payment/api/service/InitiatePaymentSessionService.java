package com.payment.api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.payment.api.dto.CreateCheckoutSessionRequest;
import com.payment.api.dto.CreateCheckoutSessionResponse;
import com.payment.api.dto.InitiatePaymentSessionRequest;
import com.payment.api.dto.InitiatePaymentSessionResponse;
import com.payment.api.enums.PaymentStatus;
import com.payment.api.model.Payment;
import com.payment.api.paymentProviders.PaymentProviderFactory;
import com.payment.api.repository.PaymentRepository;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class InitiatePaymentSessionService {

    private final PaymentRepository paymentRepository;
    private final PaymentProviderFactory providerFactory;
    private final String appId;

    public InitiatePaymentSessionService(
            PaymentRepository paymentRepository,
            PaymentProviderFactory providerFactory,
            @Value("${appId}") String appId
    ) {
        this.paymentRepository = paymentRepository;
        this.providerFactory = providerFactory;
        this.appId = appId;
    }

    @Transactional
    public InitiatePaymentSessionResponse initiateSession(InitiatePaymentSessionRequest request) {
        System.out.println("********************* InitiatePaymentSessionService called");

        // Idempotency check
        Optional<Payment> existing = paymentRepository.findByIdempotencyKeyAndAppId(
                request.getIdempotencyKey(), appId //TODO get appId from request
        );
        if (existing.isPresent()) {
            Payment payment = existing.get();
            return new InitiatePaymentSessionResponse(payment.getCheckoutUrl(), payment.getId().toString());
        }

        //  provider (Stripe, etc.)
        var provider = providerFactory.resolve(request.getProvider());

        // Build provider request
        var checkoutRequest = new CreateCheckoutSessionRequest();
        checkoutRequest.setAmount(request.getAmount());
        checkoutRequest.setCurrency(request.getCurrency());
        checkoutRequest.setIntendId(request.getIntentId());
        checkoutRequest.setUserId(request.getUserId());
        checkoutRequest.setAppId(appId);
        checkoutRequest.setIdempotencyKey(request.getIdempotencyKey());

        // Create checkout session via provider (Stripe SDK)
        CreateCheckoutSessionResponse session = provider.createCheckoutSession(checkoutRequest);

        // Save Payment entity
        Payment payment = new Payment();
        payment.setIntendId(request.getIntentId());
        payment.setUserId(request.getUserId());
        payment.setAppId(appId);
        payment.setAmount(request.getAmount());
        payment.setCurrency(request.getCurrency());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setIdempotencyKey(request.getIdempotencyKey());
        payment.setCheckoutUrl(session.getCheckoutUrl());
        payment.setCreatedAt(OffsetDateTime.now());
        payment.setProvider(request.getProvider());

        paymentRepository.save(payment);

        // Return response
        return new InitiatePaymentSessionResponse(session.getCheckoutUrl(), payment.getId().toString());
    }
}
