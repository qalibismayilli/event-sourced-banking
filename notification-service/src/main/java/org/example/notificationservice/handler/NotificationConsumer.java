package org.example.notificationservice.handler;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationConsumer {

    private final Logger log = LoggerFactory.getLogger(NotificationConsumer.class);

    @KafkaListener(topics = "account-created-events-topic", groupId = "notification-group")
    public void handleAccountCreated(String message) {
        log.info("Notification [ACCOUNT CREATED]: {}", message);
    }

    @KafkaListener(topics = "account-closed-events-topic", groupId = "notification-group")
    public void handleAccountClosed(String message) {
        log.info("Notification [ACCOUNT CLOSED]: {}", message);
    }

    @KafkaListener(topics = "account-frozen-events-topic", groupId = "notification-group")
    public void handleAccountFrozen(String message) {
        log.info("Notification [ACCOUNT FROZEN]: {}", message);
    }

    @KafkaListener(topics = "transaction-executed-events-topic", groupId = "notification-group")
    public void handleTransactionExecuted(String message) {
        log.info("Notification [TRANSACTION EXECUTED]: {}", message);
    }
}