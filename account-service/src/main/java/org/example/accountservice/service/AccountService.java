package org.example.accountservice.service;

import org.example.accountservice.dto.AccountRequestDto;
import org.example.accountservice.dto.AccountResponseDto;
import org.example.accountservice.kafka.AccountEventPublisher;
import org.example.accountservice.model.Account;
import org.example.accountservice.model.AccountStatus;
import org.example.accountservice.repository.AccountRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final AccountEventPublisher accountEventPublisher;

    public AccountService(AccountRepository accountRepository,
                          AccountEventPublisher accountEventPublisher) {
        this.accountRepository = accountRepository;
        this.accountEventPublisher = accountEventPublisher;
    }

    public AccountResponseDto createAccount(AccountRequestDto request) {
        Account account = Account.builder()
                .ownerName(request.getOwnerName())
                .currency(request.getCurrency())
                .monthlyLimit(request.getLimit() != null ? request.getLimit() : new BigDecimal("10000"))
                .balance(BigDecimal.ZERO)
                .status(AccountStatus.ACTIVE)
                .build();

        Account saved = accountRepository.save(account);
        accountEventPublisher.publishAccountCreatedEvent(saved);
        return mapToResponse(saved);
    }

    public AccountResponseDto closeAccount(UUID accountId) {
        Account account = getOriginalAccount(accountId);
        account.setStatus(AccountStatus.CLOSED);
        Account saved = accountRepository.save(account);
        accountEventPublisher.publishAccountClosedEvent(saved);
        return mapToResponse(saved);
    }

    public AccountResponseDto freezeAccount(UUID accountId) {
        Account account = getOriginalAccount(accountId);
        account.setStatus(AccountStatus.FROZEN);
        Account saved = accountRepository.save(account);
        accountEventPublisher.publishAccountFrozenEvent(saved);
        return mapToResponse(saved);
    }

    public AccountResponseDto getAccount(UUID accountId) {
        Account account = getOriginalAccount(accountId);
        return mapToResponse(account);
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
