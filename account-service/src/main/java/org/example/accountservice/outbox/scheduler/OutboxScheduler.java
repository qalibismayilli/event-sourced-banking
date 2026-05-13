package org.example.accountservice.outbox.scheduler;

import lombok.AllArgsConstructor;
import org.example.accountservice.outbox.processor.OutboxProcessor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class OutboxScheduler {
    private final OutboxProcessor outboxProcessor;

    @Scheduled(fixedDelay = 5000)
    public void schedulePending() {
        outboxProcessor.processPending();
    }

    @Scheduled(fixedDelay = 600000)
    public void scheduleFailed() {
        outboxProcessor.processFailed();
    }
}
