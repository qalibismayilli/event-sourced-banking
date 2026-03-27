package org.example.accountservice.handler;

import lombok.RequiredArgsConstructor;
import org.example.accountservice.dto.AccountResponseDto;
import org.example.accountservice.handler.consume_events.TransactionExecutedEvent;
import org.example.accountservice.service.AccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class TransactionEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(TransactionEventConsumer.class);

    private final AccountService accountService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "transaction-executed-events-topic", groupId = "account-group")
    public void handleTransactionExecuted(String message) {
        TransactionExecutedEvent event = objectMapper.readValue(message, TransactionExecutedEvent.class);

        AccountResponseDto dto = accountService.updateBalance(event);
        log.info("Received transaction executed event: {}", dto);
    }
}