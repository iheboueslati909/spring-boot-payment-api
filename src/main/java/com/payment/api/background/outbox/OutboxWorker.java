package com.payment.api.background.outbox;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OutboxWorker {

    private final OutboxProcessor processor;

    public OutboxWorker(OutboxProcessor processor) {
        this.processor = processor;
    }

    // Runs every 5 seconds (like Task.Delay in .NET)
    @Scheduled(fixedDelay = 5000)
    public void execute() {
        processor.process();
    }
}
