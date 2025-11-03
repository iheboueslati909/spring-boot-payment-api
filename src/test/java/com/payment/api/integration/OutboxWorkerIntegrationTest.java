package com.payment.api.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payment.api.background.outbox.OutboxProcessor;
import com.payment.api.messaging.EventPublisher;
import com.payment.api.messaging.contracts.PaymentProcessedEvent;
import com.payment.api.model.OutboxMessage;
import com.payment.api.repository.OutboxRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
class OutboxWorkerIntegrationTest {

    @Autowired
    private OutboxRepository outboxRepository;

    @Autowired
    private OutboxProcessor outboxProcessor;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EventPublisher eventPublisher;

    @Test
    @Transactional
    void testOutboxWorkerPublishesEvent() throws Exception {
        // 1️⃣ Persist a test outbox message
        PaymentProcessedEvent testEvent = new PaymentProcessedEvent(
                UUID.randomUUID(),
                "intent123",
                "app123",
                java.math.BigDecimal.valueOf(100),
                "user123");

        OutboxMessage message = new OutboxMessage();
        message.setType("PaymentProcessedEvent");
        message.setContent(objectMapper.writeValueAsString(testEvent));
        message.setCreatedAt(java.time.OffsetDateTime.now());

        outboxRepository.save(message);

        // 2️⃣ Trigger processing (simulate worker)
        outboxProcessor.process();

        // refEq compares field by field recursively.
        // Ignores the object reference.
        // Useful if you want to check all fields match, but the object instance is
        // different.
        //we use it because the outbox processor creates a new instance 
        //of the event when reading from the outbox.
        verify(eventPublisher, times(1)).publish(any(PaymentProcessedEvent.class));

        // 4️⃣ Verify that the message was marked as processed
        OutboxMessage processedMessage = outboxRepository.findById(message.getId()).orElseThrow();
        assert processedMessage.getProcessedAt() != null;
        assert processedMessage.getError() == null;
    }
}
