package com.payment.api.dto;

import java.math.BigDecimal;

public class CreateCheckoutSessionRequest {
    private BigDecimal amount;
    private String currency;
    private String intendId;
    private String userId;
    private String appId;
    private String idempotencyKey;

    // Getters and setters
    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getIntendId() {
        return intendId;
    }

    public void setIntendId(String intendId) {
        this.intendId = intendId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }
}
