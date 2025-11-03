package com.payment.api.configuration;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    @Value("${rabbitmq.exchange}")
    private String exchangeName;

    @Value("${rabbitmq.queue}")
    private String queueName;

    @Value("${rabbitmq.routing-key}")
    private String routingKey;

    // 1️⃣ Exchange
    @Bean
    public TopicExchange paymentExchange() {
        return new TopicExchange(exchangeName);
    }

    // 2️⃣ Queue
    @Bean
    public Queue paymentQueue() {
        return new Queue(queueName, true);
    }

    // 3️⃣ Binding
    @Bean
    public Binding paymentBinding(Queue paymentQueue, TopicExchange paymentExchange) {
        return BindingBuilder.bind(paymentQueue).to(paymentExchange).with(routingKey);
    }

    // 4️⃣ RabbitTemplate
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        return new RabbitTemplate(connectionFactory);
    }
}
