package org.example.accountservice.outbox.processor;

import lombok.AllArgsConstructor;
import org.example.accountservice.outbox.model.OutboxEventStatus;
import org.example.accountservice.outbox.model.OutboxMessage;
import org.example.accountservice.outbox.publisher.OutboxPublisher;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class OutboxProcessor {
    private final OutboxRepository outboxRepository;
    private final OutboxPublisher publisher;

    public void processPending() {
        List<OutboxMessage> messages = outboxRepository.findAndMarkAsProcessing(OutboxEventStatus.PENDING);
        if (messages.isEmpty()) {
            return;
        }
        messages.forEach(message -> {
            try {
                publisher.publish(message);
                outboxRepository.updateStatus(message.getId(), OutboxEventStatus.COMPLETED);
            } catch (Exception e) {
                if (message.getRetryCount() < 3) {
                    outboxRepository.incrementRetryAndUpdateStatus(message.getId(), OutboxEventStatus.PENDING);
                } else {
                    outboxRepository.incrementRetryAndUpdateStatus(message.getId(), OutboxEventStatus.FAILED);
                }
            }
        });
    }

    public void processFailed() {
        List<OutboxMessage> messages = outboxRepository.findAndMarkAsProcessing(OutboxEventStatus.FAILED);
        if (messages.isEmpty()) {
            return;
        }
        messages.forEach(message -> {
            try {
                publisher.publish(message);
                outboxRepository.updateStatus(message.getId(), OutboxEventStatus.COMPLETED);
            } catch (Exception e) {
                if (message.getRetryCount() < 10) {
                    outboxRepository.incrementRetryAndUpdateStatus(message.getId(), OutboxEventStatus.FAILED);
                } else {
                    outboxRepository.incrementRetryAndUpdateStatus(message.getId(), OutboxEventStatus.CANCELLED);
                }
            }
        });
    }
}
