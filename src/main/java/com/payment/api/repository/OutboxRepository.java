package com.payment.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.payment.api.model.OutboxMessage;

import java.util.List;
import java.util.UUID;

public interface OutboxRepository extends JpaRepository<OutboxMessage, UUID> {

    // Find up to 20 unprocessed messages ordered by creation time
    List<OutboxMessage> findTop20ByProcessedAtIsNullOrderByCreatedAtAsc();
}
