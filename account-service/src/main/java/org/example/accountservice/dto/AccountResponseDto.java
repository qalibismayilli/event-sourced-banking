package org.example.accountservice.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.example.accountservice.model.AccountStatus;
import org.example.accountservice.model.Currency;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AccountResponseDto {
    UUID accountId;
    String ownerName;
    BigDecimal balance;
    Currency currency;
    AccountStatus status;
    BigDecimal limit;
    LocalDateTime createdDate;
    LocalDateTime updatedDate;
}
