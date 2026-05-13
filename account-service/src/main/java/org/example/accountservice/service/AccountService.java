package org.example.accountservice.service;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.example.accountservice.dto.AccountRequestDto;
import org.example.accountservice.dto.AccountResponseDto;
import org.example.accountservice.model.Account;
import org.example.accountservice.outbox.model.EventType;
import org.example.accountservice.outbox.model.OutboxEventStatus;
import org.example.accountservice.outbox.model.OutboxMessage;
import org.example.accountservice.outbox.service.OutboxService;
import org.example.accountservice.repository.AccountRepository;
import org.example.sharedevents.event.AccountClosedEvent;
import org.example.sharedevents.event.AccountCreatedEvent;
import org.example.sharedevents.event.AccountFrozenEvent;
import org.example.sharedevents.event.TransactionExecutedEvent;
import org.example.sharedevents.util.AccountStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final OutboxService outboxService;
    private final ObjectMapper objectMapper;

    @Transactional
    public AccountResponseDto createAccount(AccountRequestDto request) {
        Account account = Account.builder()
                .ownerName(request.getOwnerName())
                .currency(request.getCurrency())
                .monthlyLimit(request.getLimit() != null ? request.getLimit() : new BigDecimal("10000"))
                .balance(BigDecimal.ZERO)
                .status(AccountStatus.ACTIVE)
                .build();
        AccountCreatedEvent event = new AccountCreatedEvent(
                account.getAccountId(),
                account.getOwnerName(),
                account.getBalance(),
                account.getCurrency(),
                account.getStatus(),
                account.getMonthlyLimit(),
                account.getCreatedDate()
        );
        OutboxMessage outboxMessage = OutboxMessage
                .builder()
                .eventType(EventType.ACCOUNT_CREATED)
                .payload(objectMapper.writeValueAsString(event))
                .status(OutboxEventStatus.PENDING)
                .retryCount(0)
                .build();
        Account saved = accountRepository.save(account);
        outboxService.save(outboxMessage);
        return mapToResponse(saved);
    }

    @Transactional
    public AccountResponseDto closeAccount(UUID accountId) {
        Account account = getOriginalAccount(accountId);
        account.setStatus(AccountStatus.CLOSED);
        AccountClosedEvent event = new AccountClosedEvent(
                account.getAccountId(),
                LocalDateTime.now()
        );
        OutboxMessage outboxMessage = OutboxMessage
                .builder()
                .eventType(EventType.ACCOUNT_CLOSED)
                .payload(objectMapper.writeValueAsString(event))
                .status(OutboxEventStatus.PENDING)
                .retryCount(0)
                .build();
        Account saved = accountRepository.save(account);
        outboxService.save(outboxMessage);
        return mapToResponse(saved);
    }

    @Transactional
    public AccountResponseDto freezeAccount(UUID accountId) {
        Account account = getOriginalAccount(accountId);
        account.setStatus(AccountStatus.FROZEN);
        AccountFrozenEvent event = new AccountFrozenEvent(
                account.getAccountId(),
                LocalDateTime.now()
        );
        OutboxMessage outboxMessage = OutboxMessage
                .builder()
                .eventType(EventType.ACCOUNT_FROZEN)
                .payload(objectMapper.writeValueAsString(event))
                .status(OutboxEventStatus.PENDING)
                .retryCount(0)
                .build();
        Account saved = accountRepository.save(account);
        outboxService.save(outboxMessage);
        return mapToResponse(saved);
    }

    public AccountResponseDto getAccount(UUID accountId) {
        Account account = getOriginalAccount(accountId);
        return mapToResponse(account);
    }

    @Transactional
    public AccountResponseDto updateBalance(@NotNull TransactionExecutedEvent event) {
        Account account = getOriginalAccount(event.getAccountId());
        switch (event.getType()) {
            case DEPOSIT -> {
                account.setBalance(account.getBalance().add(event.getAmount()));
            }
            case WITHDRAW -> {
                validateSufficientBalance(account, event.getAmount());
                account.setBalance(account.getBalance().subtract(event.getAmount()));
            }
            case TRANSFER -> {
                Account toAccount = getOriginalAccount(event.getToAccountId());
                validateSufficientBalance(account, event.getAmount());
                account.setBalance(account.getBalance().subtract(event.getAmount()));
                toAccount.setBalance(toAccount.getBalance().add(event.getAmount()));
                accountRepository.save(toAccount);
            }
            default -> throw new RuntimeException("Unknown transaction type: " + event.getType());
        }
        accountRepository.save(account);
        return mapToResponse(account);
    }

    private void validateSufficientBalance(Account account, BigDecimal amount) {
        if (account.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance");
        }
    }

    private Account getOriginalAccount(UUID accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found: " + accountId));
    }

    private AccountResponseDto mapToResponse(Account account) {
        AccountResponseDto response = new AccountResponseDto();
        response.setAccountId(account.getAccountId());
        response.setOwnerName(account.getOwnerName());
        response.setBalance(account.getBalance());
        response.setCurrency(account.getCurrency());
        response.setStatus(account.getStatus());
        response.setMonthlyLimit(account.getMonthlyLimit());
        response.setCreatedDate(account.getCreatedDate());
        response.setUpdatedDate(account.getUpdatedDate());
        return response;
    }
}
