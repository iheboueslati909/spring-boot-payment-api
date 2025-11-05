package com.payment.api.paymentProviders;

import com.payment.api.dto.CreateCheckoutSessionRequest;
import com.payment.api.dto.CreateCheckoutSessionResponse;

public interface IPaymentProvider {
    CreateCheckoutSessionResponse createCheckoutSession(CreateCheckoutSessionRequest request);
}
