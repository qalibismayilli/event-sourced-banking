package org.example.sharedevents.event;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.sharedevents.util.AccountStatus;
import org.example.sharedevents.util.Currency;


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