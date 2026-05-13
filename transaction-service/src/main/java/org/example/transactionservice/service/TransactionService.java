package org.example.transactionservice.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.sharedevents.event.TransactionExecutedEvent;
import org.example.sharedevents.util.TransactionStatus;
import org.example.sharedevents.util.TransactionType;
import org.example.transactionservice.dto.TransactionRequestDto;
import org.example.transactionservice.dto.TransactionResponseDto;
import org.example.transactionservice.model.Transaction;
import org.example.transactionservice.outbox.model.EventType;
import org.example.transactionservice.outbox.model.OutboxEventStatus;
import org.example.transactionservice.outbox.model.OutboxMessage;
import org.example.transactionservice.outbox.service.OutboxService;
import org.example.transactionservice.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final ObjectMapper objectMapper;
    private final OutboxService outboxService;

    @Transactional
    public TransactionResponseDto deposit(TransactionRequestDto request) {
        Transaction transaction = Transaction.builder()
                .accountId(request.getAccountId())
                .amount(request.getAmount())
                .type(TransactionType.DEPOSIT)
                .status(TransactionStatus.SUCCESS)
                .description(request.getDescription())
                .build();
        OutboxMessage outboxMessage = buildOutboxMessage(transaction);
        Transaction saved = transactionRepository.save(transaction);
        outboxService.save(outboxMessage);
        return mapToResponse(saved);
    }

    @Transactional
    public TransactionResponseDto withdraw(TransactionRequestDto request) {
        Transaction transaction = Transaction.builder()
                .accountId(request.getAccountId())
                .amount(request.getAmount())
                .type(TransactionType.WITHDRAW)
                .description(request.getDescription())
                .status(TransactionStatus.SUCCESS)
                .build();
        OutboxMessage outboxMessage = buildOutboxMessage(transaction);
        Transaction saved = transactionRepository.save(transaction);
        outboxService.save(outboxMessage);
        return mapToResponse(saved);
    }

    @Transactional
    public TransactionResponseDto transfer(TransactionRequestDto request) {
        if (request.getToAccountId() == null) {
            throw new RuntimeException("toAccountId is required for TRANSFER transactions");
        }
        Transaction transaction = Transaction.builder()
                .accountId(request.getAccountId())
                .toAccountId(request.getToAccountId())
                .amount(request.getAmount())
                .type(TransactionType.TRANSFER)
                .status(TransactionStatus.SUCCESS)
                .description(request.getDescription())
                .build();
        OutboxMessage outboxMessage = buildOutboxMessage(transaction);
        Transaction saved = transactionRepository.save(transaction);
        outboxService.save(outboxMessage);
        return mapToResponse(saved);
    }

    private OutboxMessage buildOutboxMessage(Transaction transaction) {
        TransactionExecutedEvent event = new TransactionExecutedEvent(
                transaction.getTransactionId(),
                transaction.getAccountId(),
                transaction.getToAccountId(),
                transaction.getAmount(),
                transaction.getType(),
                transaction.getStatus(),
                transaction.getCreatedDate()
        );
        return OutboxMessage.builder()
                .eventType(EventType.TRANSACTION_EXECUTED)
                .payload(objectMapper.writeValueAsString(event))
                .status(OutboxEventStatus.PENDING)
                .retryCount(0)
                .build();
    }

    private TransactionResponseDto mapToResponse(Transaction transaction) {
        TransactionResponseDto response = new TransactionResponseDto();
        response.setTransactionId(transaction.getTransactionId());
        response.setAccountId(transaction.getAccountId());
        response.setToAccountId(transaction.getToAccountId());
        response.setAmount(transaction.getAmount());
        response.setType(transaction.getType());
        response.setStatus(transaction.getStatus());
        response.setDescription(transaction.getDescription());
        response.setCreatedDate(transaction.getCreatedDate());
        return response;
    }
}