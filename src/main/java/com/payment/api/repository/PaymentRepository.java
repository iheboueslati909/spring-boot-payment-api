package com.payment.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.payment.api.model.Payment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    // Find a payment by its idempotency key (useful to prevent duplicates)
    Optional<Payment> findByIdempotencyKey(String idempotencyKey);

    // Optional: find payments by user
    List<Payment> findByUserIdOrderByCreatedAtDesc(String userId);

    Optional<Payment> findByIntendIdAndAppId(String intendId,String appId);
    Optional<Payment> findByIdempotencyKeyAndAppId(String idempotencyKey, String appId);
}
