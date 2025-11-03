package com.payment.api.background.outbox;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.payment.api.configuration.RabbitMqConfig;
import com.payment.api.messaging.EventPublisher;
import com.payment.api.messaging.contracts.PaymentProcessedEvent;
import com.payment.api.model.OutboxMessage;
import com.payment.api.repository.OutboxRepository;

import java.util.List;

@Service
public class OutboxProcessor {

    private final OutboxRepository outboxRepository;
    private final EventPublisher eventPublisher;  // abstracted
    private final ObjectMapper objectMapper;

    public OutboxProcessor(OutboxRepository outboxRepository, EventPublisher eventPublisher, ObjectMapper objectMapper) {
        this.outboxRepository = outboxRepository;
        this.eventPublisher = eventPublisher;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void process() {
        List<OutboxMessage> messages = outboxRepository.findTop20ByProcessedAtIsNullOrderByCreatedAtAsc();

        for (OutboxMessage message : messages) {
            try {
                switch (message.getType()) {
                    case "PaymentProcessedEvent" -> {
                        PaymentProcessedEvent event = objectMapper.readValue(message.getContent(), PaymentProcessedEvent.class);
                        eventPublisher.publish(event);
                        System.out.println("Published PaymentProcessedEvent: " + event);
                    }
                    // other events...
                }

                message.setProcessedAt(java.time.OffsetDateTime.now());
            } catch (Exception ex) {
                message.setError(ex.getMessage());
            }
        }

        outboxRepository.saveAll(messages);
    }
}
