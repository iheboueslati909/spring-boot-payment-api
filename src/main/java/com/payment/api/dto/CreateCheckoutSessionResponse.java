package com.payment.api.dto;

public class CreateCheckoutSessionResponse {
    private String checkoutUrl;
    private String sessionId;

    // Getters and setters
    public String getCheckoutUrl() {
        return checkoutUrl;
    }

    public void setCheckoutUrl(String checkoutUrl) {
        this.checkoutUrl = checkoutUrl;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
}
