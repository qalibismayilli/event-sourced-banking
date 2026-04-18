package org.example.transactionservice.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.example.sharedevents.util.TransactionStatus;
import org.example.sharedevents.util.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TransactionResponseDto {

    UUID transactionId;
    UUID accountId;
    UUID toAccountId;
    BigDecimal amount;
    TransactionType type;
    TransactionStatus status;
    String description;
    LocalDateTime createdDate;
}