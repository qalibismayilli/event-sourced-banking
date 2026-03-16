package org.example.accountservice.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.example.accountservice.dto.AccountRequestDto;
import org.example.accountservice.dto.AccountResponseDto;
import org.example.accountservice.service.AccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/accounts")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<AccountResponseDto> createAccount(@RequestBody @Valid AccountRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(accountService.createAccount(request));
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<AccountResponseDto> getAccount(@PathVariable UUID accountId) {
        return ResponseEntity.ok(accountService.getAccount(accountId));
    }

    @PatchMapping("/{accountId}/close")
    public ResponseEntity<AccountResponseDto> closeAccount(@PathVariable UUID accountId) {
        return ResponseEntity.ok(accountService.closeAccount(accountId));
    }

    @PatchMapping("/{accountId}/freeze")
    public ResponseEntity<AccountResponseDto> freezeAccount(@PathVariable UUID accountId) {
        return ResponseEntity.ok(accountService.freezeAccount(accountId));
    }
}