package com.payment.api.paymentProviders;

import com.payment.api.dto.CreateCheckoutSessionRequest;
import com.payment.api.dto.CreateCheckoutSessionResponse;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import com.stripe.param.checkout.SessionCreateParams.PaymentMethodType;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class StripePaymentProvider implements IPaymentProvider {

    public StripePaymentProvider(@Value("${stripe.api-key}") String apiKey) {
        Stripe.apiKey = apiKey;
    }

    @Override
    public CreateCheckoutSessionResponse createCheckoutSession(CreateCheckoutSessionRequest request) {
        try {
            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .addPaymentMethodType(PaymentMethodType.CARD)
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setQuantity(1L)
                                    .setPriceData(
                                            SessionCreateParams.LineItem.PriceData.builder()
                                                    .setCurrency(request.getCurrency())
                                                    .setUnitAmount(request.getAmount().multiply(new java.math.BigDecimal(100)).longValue())
                                                    .setProductData(
                                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                    .setName("Payment")
                                                                    .build()
                                                    )
                                                    .build()
                                    )
                                    .build()
                    )
                    .putMetadata("IntentId", request.getIntendId())
                    .putMetadata("UserId", request.getUserId())
                    .putMetadata("AppId", request.getAppId())
                    .putMetadata("IdempotencyKey", request.getIdempotencyKey()) //TODO REPLACE SUCCESS URL
                    .setSuccessUrl("https://yourdomain.com/payment/success?session_id={CHECKOUT_SESSION_ID}")
                    .setCancelUrl("https://yourdomain.com/payment/cancel")
                    .build();

            Session session = Session.create(params);

            return new CreateCheckoutSessionResponse(session.getUrl(), session.getId());

        } catch (StripeException e) {
            throw new RuntimeException("Failed to create Stripe checkout session", e);
        }
    }
}
