package com.payment.api.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RabbitMqEventPublisher implements EventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange}")
    private String exchange;

    @Value("${rabbitmq.routing-key}")
    private String paymentRoutingKey;

    public RabbitMqEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publish(Object event) {
        String routingKey = switch (event.getClass().getSimpleName()) {
            case "PaymentProcessedEvent" -> paymentRoutingKey;
            default -> "default";
        };

        rabbitTemplate.convertAndSend(exchange, routingKey, event);
    }
}

