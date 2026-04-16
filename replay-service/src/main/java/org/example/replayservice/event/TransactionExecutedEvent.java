package org.example.replayservice.event;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TransactionExecutedEvent {
    UUID transactionId;
    UUID accountId;
    UUID toAccountId;
    BigDecimal amount;
    TransactionType type;
    TransactionStatus status;
    LocalDateTime createdDate;
}