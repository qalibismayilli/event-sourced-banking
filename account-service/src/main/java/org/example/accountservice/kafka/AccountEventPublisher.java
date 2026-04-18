package org.example.accountservice.kafka;

import lombok.RequiredArgsConstructor;
import org.example.accountservice.config.KafkaConfig;
import org.example.accountservice.model.Account;
import org.example.sharedevents.event.AccountClosedEvent;
import org.example.sharedevents.event.AccountCreatedEvent;
import org.example.sharedevents.event.AccountFrozenEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AccountEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishAccountCreatedEvent(Account account) {
        AccountCreatedEvent event = new AccountCreatedEvent(
                account.getAccountId(),
                account.getOwnerName(),
                account.getBalance(),
                account.getCurrency(),
                account.getStatus(),
                account.getMonthlyLimit(),
                account.getCreatedDate()
        );
        kafkaTemplate.send(
                KafkaConfig.ACCOUNT_CREATED_TOPIC,
                account.getAccountId().toString(),
                event
        );
    }

    public void publishAccountClosedEvent(Account account) {
        AccountClosedEvent event = new AccountClosedEvent(
                account.getAccountId(),
                LocalDateTime.now()
        );
        kafkaTemplate.send(
                KafkaConfig.ACCOUNT_CLOSED_TOPIC,
                account.getAccountId().toString(),
                event
        );
    }

    public void publishAccountFrozenEvent(Account account) {
        AccountFrozenEvent event = new AccountFrozenEvent(
                account.getAccountId(),
                LocalDateTime.now()
        );
        kafkaTemplate.send(
                KafkaConfig.ACCOUNT_FROZEN_TOPIC,
                account.getAccountId().toString(),
                event
        );
    }
}