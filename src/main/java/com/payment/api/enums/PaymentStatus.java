package com.payment.api.enums;

public enum PaymentStatus {
    PENDING(0),
    SUCCESSFUL(1),
    FAILED(2),
    REFUNDED(3);

    private final int code;

    PaymentStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    // Optional: reverse lookup by code
    public static PaymentStatus fromCode(int code) {
        for (PaymentStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid PaymentStatus code: " + code);
    }
}
