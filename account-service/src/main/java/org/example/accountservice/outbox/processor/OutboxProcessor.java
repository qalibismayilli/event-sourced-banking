package org.example.accountservice.outbox.processor;

import lombok.AllArgsConstructor;
import org.example.accountservice.outbox.model.OutboxEventStatus;
import org.example.accountservice.outbox.model.OutboxMessage;
import org.example.accountservice.outbox.publisher.OutboxPublisher;
import org.example.accountservice.outbox.repository.OutboxRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class OutboxProcessor {
    private final OutboxRepository outboxRepository;
    private final OutboxPublisher publisher;

    @Transactional
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
                    outboxRepository.incrementRetryAndUpdateStatusAndSetErrorMessage(
                            message.getId(),
                            OutboxEventStatus.PENDING,
                            e.getMessage()
                    );
                } else {
                    outboxRepository.incrementRetryAndUpdateStatusAndSetErrorMessage(
                            message.getId(),
                            OutboxEventStatus.FAILED,
                            e.getMessage()
                    );
                }
            }
        });
    }

    @Transactional
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
                    outboxRepository.incrementRetryAndUpdateStatusAndSetErrorMessage(
                            message.getId(),
                            OutboxEventStatus.FAILED,
                            e.getMessage()
                    );
                } else {
                    outboxRepository.incrementRetryAndUpdateStatusAndSetErrorMessage(
                            message.getId(),
                            OutboxEventStatus.CANCELLED,
                            e.getMessage()
                    );
                }
            }
        });
    }
}
