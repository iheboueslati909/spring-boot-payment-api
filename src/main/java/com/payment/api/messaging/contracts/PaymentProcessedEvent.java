package com.payment.api.messaging.contracts;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public class PaymentProcessedEvent {

    private UUID paymentId;
    private String intentId;
    private String appId;
    private BigDecimal amount;
    private String userId;
    private OffsetDateTime processedAt;
    private String status;

    public PaymentProcessedEvent(UUID paymentId, String intentId, String appId, BigDecimal amount, String userId) {
        this.paymentId = paymentId;
        this.intentId = intentId;
        this.appId = appId;
        this.amount = amount;
        this.userId = userId;
        this.processedAt = OffsetDateTime.now();
        this.status = "Succeeded";
    }

    // getters and setters
    public UUID getPaymentId() { return paymentId; }
    public String getIntentId() { return intentId; }
    public String getAppId() { return appId; }
    public BigDecimal getAmount() { return amount; }
    public String getUserId() { return userId; }
    public OffsetDateTime getProcessedAt() { return processedAt; }
    public String getStatus() { return status; }

    public void setProcessedAt(OffsetDateTime processedAt) { this.processedAt = processedAt; }
    public void setStatus(String status) { this.status = status; }
}
