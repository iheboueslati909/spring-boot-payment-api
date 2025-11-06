package com.payment.api.dto;

import java.math.BigDecimal;

import com.payment.api.enums.PaymentProvider;

public class InitiatePaymentSessionRequest {

    private BigDecimal amount;
    private String currency;
    private String intentId;
    private String userId;
    private String idempotencyKey;
    private PaymentProvider provider;

    // Getters / Setters
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getIntentId() { return intentId; }
    public void setIntentId(String intentId) { this.intentId = intentId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }

    public PaymentProvider getProvider() { return provider; }
    public void setProvider(PaymentProvider provider) { this.provider = provider; }
}
