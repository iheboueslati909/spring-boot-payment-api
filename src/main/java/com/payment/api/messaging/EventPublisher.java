package com.payment.api.messaging;

public interface EventPublisher {

    /**
     * Publishes a domain event to the message broker.
     */
    void publish(Object event);
}
