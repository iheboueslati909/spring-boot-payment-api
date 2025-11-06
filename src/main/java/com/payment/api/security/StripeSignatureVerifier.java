package com.payment.api.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Component
public class StripeSignatureVerifier {

    private static final Logger logger = LoggerFactory.getLogger(StripeSignatureVerifier.class);
    private static final String HMAC_SHA256 = "HmacSHA256";

    @Value("${stripe.webhook.secret}")
    private String defaultWebhookSecret;

    public String getEndpointSecret() {
        return defaultWebhookSecret;
    }

    public boolean verify(String payload, String signatureHeader) {
        String secret = getEndpointSecret();

        if (!StringUtils.hasText(payload) || !StringUtils.hasText(signatureHeader) || !StringUtils.hasText(secret)) {
            logger.warn("Missing payload, signature header, or webhook secret.");
            return false;
        }

        Map<String, String> parsed = parseStripeSignatureHeader(signatureHeader);
        String timestamp = parsed.get("t");
        String signature = parsed.get("v1");

        if (timestamp == null || signature == null) {
            logger.warn("Invalid Stripe signature header format: {}", signatureHeader);
            return false;
        }

        try {
            String signedPayload = timestamp + "." + payload;

            Mac mac = Mac.getInstance(HMAC_SHA256);
            SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
            mac.init(keySpec);
            byte[] computedHash = mac.doFinal(signedPayload.getBytes(StandardCharsets.UTF_8));

            String computedSignature = bytesToHex(computedHash);

            boolean valid = slowEquals(computedSignature, signature);
            if (!valid) {
                logger.warn("Stripe signature verification failed. Computed: {}, Received: {}", computedSignature, signature);
            }

            return valid;

        } catch (Exception e) {
            logger.error("Error verifying Stripe signature", e);
            return false;
        }
    }

    private Map<String, String> parseStripeSignatureHeader(String header) {
        Map<String, String> values = new HashMap<>();
        String[] parts = header.split(",");
        for (String part : parts) {
            String[] kv = part.split("=", 2);
            if (kv.length == 2) {
                values.put(kv[0].trim(), kv[1].trim());
            }
        }
        return values;
    }

    private static boolean slowEquals(String a, String b) {
        if (a == null || b == null || a.length() != b.length()) return false;
        int diff = 0;
        for (int i = 0; i < a.length(); i++) {
            diff |= a.charAt(i) ^ b.charAt(i);
        }
        return diff == 0;
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder hex = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }
}
