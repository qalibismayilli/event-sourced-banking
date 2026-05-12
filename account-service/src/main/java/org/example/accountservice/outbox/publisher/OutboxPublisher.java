package org.example.accountservice.outbox.publisher;

import lombok.RequiredArgsConstructor;
import org.example.accountservice.outbox.model.OutboxMessage;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OutboxPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publish(OutboxMessage message) {
//        kafkaTemplate.send(message.getTopic(), message.getPayload());
    }
}
