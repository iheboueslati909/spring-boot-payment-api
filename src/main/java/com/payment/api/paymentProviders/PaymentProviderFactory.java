package com.payment.api.paymentProviders;
import org.springframework.stereotype.Component;

import com.payment.api.enums.PaymentProvider;

@Component
public class PaymentProviderFactory {

    private final StripePaymentProvider stripePaymentProvider;

    public PaymentProviderFactory(StripePaymentProvider stripePaymentProvider) {
        this.stripePaymentProvider = stripePaymentProvider;
    }

    public IPaymentProvider resolve(PaymentProvider provider) {
        return switch (provider) {
            case STRIPE -> stripePaymentProvider;
            case PAYPAL -> throw new UnsupportedOperationException("PayPal not yet supported");
        };
    }
}
