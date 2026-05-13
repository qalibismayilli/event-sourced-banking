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

@Component
@RequiredArgsConstructor
public class OutboxPublisher {

    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publish(OutboxMessage message) {
        switch(message.getEventType()) {
            case ACCOUNT_CREATED:
                publishAccountCreatedEvent(objectMapper.convertValue(message.getPayload(), AccountCreatedEvent.class));
                break;
            case ACCOUNT_CLOSED:
                publishAccountClosedEvent(objectMapper.convertValue(message.getPayload(), AccountClosedEvent.class));
                break;
            case ACCOUNT_FROZEN:
                publishAccountFrozenEvent(objectMapper.convertValue(message.getPayload(), AccountFrozenEvent.class));
                break;
        }
    }

    public void publishAccountCreatedEvent(AccountCreatedEvent event) {
        kafkaTemplate.send(
                KafkaConfig.ACCOUNT_CREATED_TOPIC,
                event.getAccountId().toString(),
                event
        );
    }

    public void publishAccountClosedEvent(AccountClosedEvent event) {
        kafkaTemplate.send(
                KafkaConfig.ACCOUNT_CLOSED_TOPIC,
                event.getAccountId().toString(),
                event
        );
    }

    public void publishAccountFrozenEvent(AccountFrozenEvent event) {
        kafkaTemplate.send(
                KafkaConfig.ACCOUNT_FROZEN_TOPIC,
                event.getAccountId().toString(),
                event
        );
    }
}
