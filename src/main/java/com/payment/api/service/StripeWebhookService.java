package com.payment.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.payment.api.dto.StripeWebhookResponse;
import com.payment.api.enums.PaymentStatus;
import com.payment.api.messaging.contracts.PaymentProcessedEvent;
import com.payment.api.model.OutboxMessage;
import com.payment.api.model.Payment;
import com.payment.api.repository.OutboxRepository;
import com.payment.api.repository.PaymentRepository;
import com.payment.api.security.StripeSignatureVerifier;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Optional;

@Service
public class StripeWebhookService {

    private static final Logger logger = LoggerFactory.getLogger(StripeWebhookService.class);
    
    private final PaymentRepository paymentRepository;
    private final OutboxRepository outboxRepository;
    private final StripeSignatureVerifier signatureVerifier;
    private final ObjectMapper objectMapper;

    public StripeWebhookService(
            PaymentRepository paymentRepository,
            OutboxRepository outboxRepository,
            StripeSignatureVerifier signatureVerifier,
            ObjectMapper objectMapper

    ) {
        this.paymentRepository = paymentRepository;
        this.outboxRepository = outboxRepository;
        this.signatureVerifier = signatureVerifier;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public StripeWebhookResponse handleWebhook(String payload, String sigHeader) throws IOException {
         
        String endpointSecret = signatureVerifier.getEndpointSecret();

        Event event;
        try {
              event = Webhook.constructEvent(
                payload, 
                sigHeader, 
                endpointSecret
            );
        } catch (SignatureVerificationException e) {
            logger.warn("Stripe signature verification failed: {}", e.getMessage());
            return StripeWebhookResponse.failed("SignatureVerificationFailed");
        } catch (StripeException | NullPointerException e) {
            logger.warn("Invalid Stripe event: {}", e.getMessage());
            return StripeWebhookResponse.failed("InvalidEvent");
        }

        switch (event.getType()) {
            case "checkout.session.completed":
            case "payment_intent.succeeded":
                return handlePaymentSucceeded(event);
            case "payment_intent.payment_failed":
                return handlePaymentFailed(event);
            default:
                logger.info("Unhandled Stripe event type: {}", event.getType());
                return new StripeWebhookResponse(
                        null, null, null, event.getType(), null, null, null, "IGNORED"
                );
        }
    }

    private StripeWebhookResponse handlePaymentSucceeded(Event event) {
        PaymentIntent paymentIntent = null;
        Session session = null;
        String intendId = null;
        String appId = null;

        if ("payment_intent.succeeded".equals(event.getType())) {
            paymentIntent = (PaymentIntent) event.getDataObjectDeserializer().getObject().orElse(null);
            if (paymentIntent != null) {
                intendId = paymentIntent.getMetadata().get("IntendId");
                appId = paymentIntent.getMetadata().get("AppId");
            }
        } else if ("checkout.session.completed".equals(event.getType())) {
            session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);
            if (session != null) {
                intendId = session.getMetadata().get("IntendId");
                appId = session.getMetadata().get("AppId");
            }
        }

        Optional<Payment> paymentOpt = paymentRepository.findByIntendIdAndAppId(intendId, appId);
        if (paymentOpt.isEmpty()) {
            logger.warn("No payment found for intendId={} appId={}", intendId, appId);
            return StripeWebhookResponse.failed("PaymentNotFound");
        }

        Payment payment = paymentOpt.get();
        payment.setStatus(PaymentStatus.SUCCESSFUL);
        paymentRepository.save(payment);

        PaymentProcessedEvent processedEvent = new PaymentProcessedEvent(
            payment.getId(),
            intendId,
            appId,
            payment.getAmount(),
            payment.getUserId().toString()
        );
        
        String paymentContent = null;

        try {
            paymentContent = objectMapper.writeValueAsString(processedEvent);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        
        OutboxMessage outbox =new OutboxMessage();
        outbox.setType("PaymentProcessedEvent");
        outbox.setContent(paymentContent);

        outboxRepository.save(outbox);

        return new StripeWebhookResponse(
                payment.getId().toString(),
                null, // sessionId
                paymentIntent != null ? paymentIntent.getCustomer() :
                        session != null ? session.getCustomer() : null,
                event.getType(),
                paymentIntent != null ? paymentIntent.getId() : null,
                session != null ? session.getId() : null,
                paymentIntent != null ? paymentIntent.getPaymentMethod() : null,
                PaymentStatus.SUCCESSFUL.name()
        );
    }

    private StripeWebhookResponse handlePaymentFailed(Event event) {
        PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer().getObject().orElse(null);
        if (paymentIntent == null) return StripeWebhookResponse.failed("NoPaymentIntent");

        String intendId = paymentIntent.getMetadata().getOrDefault("IntendId", null);
        String appId = paymentIntent.getMetadata().getOrDefault("AppId", null);

        Optional<Payment> paymentOpt = paymentRepository.findByIntendIdAndAppId(intendId, appId);
        if (paymentOpt.isEmpty()) {
            logger.warn("No payment found for intendId={} appId={}", intendId, appId);
            return StripeWebhookResponse.failed("PaymentNotFound");
        }

        Payment payment = paymentOpt.get();
        payment.setStatus(PaymentStatus.FAILED);
        paymentRepository.save(payment);

        PaymentProcessedEvent processedEvent = new PaymentProcessedEvent(
            payment.getId(),
            intendId,
            appId,
            payment.getAmount(),
            payment.getUserId().toString()
        );
        
        ObjectMapper objectMapper = new ObjectMapper();
        String paymentContent = null;

        try {
            paymentContent = objectMapper.writeValueAsString(processedEvent);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        
        OutboxMessage outbox =new OutboxMessage();
        outbox.setType("PaymentProcessedEvent");
        outbox.setContent(paymentContent);

        outboxRepository.save(outbox);

        return new StripeWebhookResponse(
                payment.getId().toString(),
                null,
                paymentIntent.getCustomer(),
                event.getType(),
                paymentIntent.getId(),
                null,
                paymentIntent.getPaymentMethod(),
                PaymentStatus.FAILED.name()
        );
    }

    private String readBody(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
        }
        return sb.toString();
    }
}
