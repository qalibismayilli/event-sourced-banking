package org.example.transactionservice.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.transactionservice.dto.TransactionRequestDto;
import org.example.transactionservice.dto.TransactionResponseDto;
import org.example.transactionservice.kafka.TransactionEventPublisher;
import org.example.transactionservice.model.Transaction;
import org.example.transactionservice.model.TransactionStatus;
import org.example.transactionservice.model.TransactionType;
import org.example.transactionservice.repository.TransactionRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TransactionService {

    TransactionRepository transactionRepository;
    TransactionEventPublisher transactionEventPublisher;

    public TransactionResponseDto deposit(TransactionRequestDto request) {
        Transaction transaction = Transaction.builder()
                .accountId(request.getAccountId())
                .amount(request.getAmount())
                .type(TransactionType.DEPOSIT)
                .status(TransactionStatus.SUCCESS)
                .description(request.getDescription())
                .build();

        Transaction saved = transactionRepository.save(transaction);
        transactionEventPublisher.publishTransactionExecutedEvent(saved);
        return mapToResponse(saved);
    }

    public TransactionResponseDto withdraw(TransactionRequestDto request) {
        Transaction transaction = Transaction.builder()
                .accountId(request.getAccountId())
                .amount(request.getAmount())
                .type(TransactionType.WITHDRAW)
                .description(request.getDescription())
                .status(TransactionStatus.SUCCESS)
                .build();

        Transaction saved = transactionRepository.save(transaction);
        transactionEventPublisher.publishTransactionExecutedEvent(saved);
        return mapToResponse(saved);
    }

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

        Transaction saved = transactionRepository.save(transaction);
        transactionEventPublisher.publishTransactionExecutedEvent(saved);
        return mapToResponse(saved);
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