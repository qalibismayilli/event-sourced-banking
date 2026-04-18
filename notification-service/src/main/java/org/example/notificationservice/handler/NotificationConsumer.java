package org.example.notificationservice.handler;


import org.example.sharedevents.event.AccountClosedEvent;
import org.example.sharedevents.event.AccountCreatedEvent;
import org.example.sharedevents.event.AccountFrozenEvent;
import org.example.sharedevents.event.TransactionExecutedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class NotificationConsumer {

    private final Logger log = LoggerFactory.getLogger(NotificationConsumer.class);

    @KafkaListener(topics = "account-created-events-topic")
    public void handleAccountCreated(@Payload AccountCreatedEvent event) {
        log.info("Notification [ACCOUNT CREATED]: {}", event);
    }

    @KafkaListener(topics = "account-closed-events-topic")
    public void handleAccountClosed(@Payload AccountClosedEvent event) {
        log.info("Notification [ACCOUNT CLOSED]: {}", event);
    }

    @KafkaListener(topics = "account-frozen-events-topic")
    public void handleAccountFrozen(@Payload AccountFrozenEvent event) {
        log.info("Notification [ACCOUNT FROZEN]: {}", event);
    }

    @KafkaListener(topics = "transaction-executed-events-topic")
    public void handleTransactionExecuted(@Payload TransactionExecutedEvent event) {
        log.info("Notification [TRANSACTION EXECUTED]: {}", event);
    }
}