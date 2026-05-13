package org.example.transactionservice.outbox.publisher;

import lombok.RequiredArgsConstructor;
import org.example.sharedevents.event.TransactionExecutedEvent;
import org.example.transactionservice.config.KafkaConfig;
import org.example.transactionservice.outbox.model.OutboxMessage;
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
            case TRANSACTION_EXECUTED:
                publishTransactionExecutedEvent(objectMapper.convertValue(message.getPayload(), TransactionExecutedEvent.class));
                break;
        }
    }

    public void publishTransactionExecutedEvent(TransactionExecutedEvent event) {
        kafkaTemplate.send(
                KafkaConfig.TRANSACTION_EXECUTED_TOPIC,
                event.getAccountId().toString(),
                event
        );
    }
}
