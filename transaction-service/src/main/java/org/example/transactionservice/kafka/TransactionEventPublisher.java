package org.example.transactionservice.kafka;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.transactionservice.config.KafkaConfig;
import org.example.transactionservice.event.TransactionExecutedEvent;
import org.example.transactionservice.model.Transaction;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TransactionEventPublisher {

    KafkaTemplate<String, String> kafkaTemplate;
    ObjectMapper objectMapper;

    public void publishTransactionExecutedEvent(Transaction transaction) {
        TransactionExecutedEvent event = new TransactionExecutedEvent(
                transaction.getTransactionId(),
                transaction.getAccountId(),
                transaction.getToAccountId(),
                transaction.getAmount(),
                transaction.getType(),
                transaction.getStatus(),
                transaction.getCreatedDate()
        );
        kafkaTemplate.send(KafkaConfig.TRANSACTION_EXECUTED_TOPIC,
                transaction.getAccountId().toString(),
                objectMapper.writeValueAsString(event));
    }
}