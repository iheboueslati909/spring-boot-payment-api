package com.payment.api.dto;

public record StripeWebhookResponse(
        String paymentId,
        String sessionId,
        String customerId,
        String eventType,
        String paymentIntentId,
        String checkoutSessionId,
        String paymentMethodId,
        String paymentStatus
) {
    public static StripeWebhookResponse failed(String eventType) {
        return new StripeWebhookResponse(
                null,
                null,
                null,
                eventType,
                null,
                null,
                null,
                "FAILED"
        );
    }
}
