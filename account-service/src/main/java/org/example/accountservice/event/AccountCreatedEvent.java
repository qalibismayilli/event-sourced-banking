package org.example.accountservice.event;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.accountservice.model.AccountStatus;
import org.example.accountservice.model.Currency;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AccountCreatedEvent {
    UUID accountId;
    String ownerName;
    BigDecimal balance;
    Currency currency;
    AccountStatus status;
    BigDecimal limit;
    LocalDateTime createdDate;
}