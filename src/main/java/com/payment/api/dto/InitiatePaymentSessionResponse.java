package com.payment.api.dto;

public class InitiatePaymentSessionResponse {

    private String checkoutUrl;
    private String paymentId;

    public InitiatePaymentSessionResponse() {}

    public InitiatePaymentSessionResponse(String checkoutUrl, String paymentId) {
        this.checkoutUrl = checkoutUrl;
        this.paymentId = paymentId;
    }

    public String getCheckoutUrl() { return checkoutUrl; }
    public void setCheckoutUrl(String checkoutUrl) { this.checkoutUrl = checkoutUrl; }

    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }
}
