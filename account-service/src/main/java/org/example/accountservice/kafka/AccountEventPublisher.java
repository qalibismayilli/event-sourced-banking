package org.example.accountservice.kafka;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.accountservice.event.AccountClosedEvent;
import org.example.accountservice.event.AccountCreatedEvent;
import org.example.accountservice.event.AccountFrozenEvent;
import org.example.accountservice.model.Account;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AccountEventPublisher {

    KafkaTemplate<String, Object> kafkaTemplate;

    static final String ACCOUNT_CREATED_TOPIC = "account-created-events";
    static final String ACCOUNT_CLOSED_TOPIC = "account-closed-events";
    static final String ACCOUNT_FROZEN_TOPIC = "account-frozen-events";

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
        kafkaTemplate.send(ACCOUNT_CREATED_TOPIC, account.getAccountId().toString(), event);
    }

    public void publishAccountClosedEvent(Account account) {
        AccountClosedEvent event = new AccountClosedEvent(
                account.getAccountId(),
                LocalDateTime.now()
        );
        kafkaTemplate.send(ACCOUNT_CLOSED_TOPIC, account.getAccountId().toString(), event);
    }

    public void publishAccountFrozenEvent(Account account) {
        AccountFrozenEvent event = new AccountFrozenEvent(
                account.getAccountId(),
                LocalDateTime.now()
        );
        kafkaTemplate.send(ACCOUNT_FROZEN_TOPIC, account.getAccountId().toString(), event);
    }
}