package org.example.transactionservice.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.example.sharedevents.util.TransactionStatus;
import org.example.sharedevents.util.TransactionType;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Transaction {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.RANDOM)
    @Column(name = "transaction_id", updatable = false, nullable = false)
    UUID transactionId;

    @Column(name = "account_id", nullable = false)
    UUID accountId;

    @Column(name = "to_account_id")
    UUID toAccountId;

    @Column(nullable = false, columnDefinition = "NUMERIC CHECK (amount > 0)")
    BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR DEFAULT 'SUCCESS'")
    TransactionStatus status;

    @Column(name = "description")
    String description;

    @CreationTimestamp
    @Column(name = "created_date", updatable = false)
    LocalDateTime createdDate;
}
