package org.example.accountservice.outbox.publisher;

import lombok.RequiredArgsConstructor;
import org.example.accountservice.config.KafkaConfig;
import org.example.accountservice.outbox.model.OutboxMessage;
import org.example.sharedevents.event.AccountClosedEvent;
import org.example.sharedevents.event.AccountCreatedEvent;
import org.example.sharedevents.event.AccountFrozenEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
@RequiredArgsConstructor
public class OutboxPublisher {

    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publish(OutboxMessage message) {
        switch(message.getEventType()) {
            case ACCOUNT_CREATED:
                publishEventAndWait(KafkaConfig.ACCOUNT_CREATED_TOPIC,
                        objectMapper.convertValue(message.getPayload(), AccountCreatedEvent.class));
                break;
            case ACCOUNT_CLOSED:
                publishEventAndWait(KafkaConfig.ACCOUNT_CLOSED_TOPIC,
                        objectMapper.convertValue(message.getPayload(), AccountClosedEvent.class));
                break;
            case ACCOUNT_FROZEN:
                publishEventAndWait(KafkaConfig.ACCOUNT_FROZEN_TOPIC,
                        objectMapper.convertValue(message.getPayload(), AccountFrozenEvent.class));
                break;
        }
    }

    private void publishEventAndWait(String topic, Object event) {
        try {
            kafkaTemplate.send(topic, event).get(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while publishing event", e);
        } catch (ExecutionException | TimeoutException e) {
            throw new IllegalStateException("Failed to publish event", e);
        }
    }

}
