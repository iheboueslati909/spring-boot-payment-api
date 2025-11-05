package com.payment.api.endToEnd;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payment.api.background.outbox.OutboxProcessor;
import com.payment.api.messaging.contracts.PaymentProcessedEvent;
import com.payment.api.model.OutboxMessage;
import com.payment.api.repository.OutboxRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Component;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class OutboxWorkerEndToEndTest {

    @Container
    static RabbitMQContainer rabbitContainer = new RabbitMQContainer("rabbitmq:3.11-management");

    @DynamicPropertySource
    static void configureRabbitMQ(DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.host", rabbitContainer::getHost);
        registry.add("spring.rabbitmq.port", rabbitContainer::getAmqpPort);
            registry.add("spring.rabbitmq.exchange", () -> "payment-exchange");
    registry.add("spring.rabbitmq.routing-key", () -> "payment.routing.key");
    }

    @Autowired
    private OutboxRepository outboxRepository;

    @Autowired
    private OutboxProcessor outboxProcessor;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Queue paymentQueue;

    private final BlockingQueue<PaymentProcessedEvent> receivedMessages = new LinkedBlockingQueue<>();

    
    @BeforeEach
    void clearQueue() {
        receivedMessages.clear();
    }

    @RabbitListener(queues = "#{paymentQueue.name}")
    public void handlePaymentEvent(PaymentProcessedEvent event) {
        receivedMessages.add(event);
    }

    @Test
    void outboxWorkerPublishesEvent() throws Exception {
        
        // Persist test message
        UUID paymentId = UUID.randomUUID();
        PaymentProcessedEvent testEvent = new PaymentProcessedEvent(
                paymentId,
                "intent123",
                "app123",
                BigDecimal.valueOf(100),
                "user123"
        );

        OutboxMessage message = new OutboxMessage();
        message.setType("PaymentProcessedEvent");
        message.setContent(objectMapper.writeValueAsString(testEvent));
        message.setCreatedAt(OffsetDateTime.now());

        outboxRepository.saveAndFlush(message);

        // Trigger processor
        outboxProcessor.process();

        // Wait for message in RabbitMQ
        PaymentProcessedEvent received = receivedMessages.poll(10, TimeUnit.SECONDS);
        assertThat(received).isNotNull();
        assertThat(received.getPaymentId()).isEqualTo(paymentId);

        // Verify outbox marked processed
        OutboxMessage processedMessage = outboxRepository.findById(message.getId()).orElseThrow();
        assertThat(processedMessage.getProcessedAt()).isNotNull();
        assertThat(processedMessage.getError()).isNull();
    }
}
