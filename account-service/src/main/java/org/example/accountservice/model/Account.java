package org.example.accountservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "accounts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Account {
    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.RANDOM)
    @Column(name = "account_id", updatable = false, nullable = false)
    UUID accountId;

    @CreationTimestamp
    @Column(name = "created_date")
    LocalDateTime createdDate;

    @UpdateTimestamp
    @Column(name = "updated_date")
    LocalDateTime updatedDate;

    @Column(name = "owner_name")
    String ownerName;

    @Column(nullable = false, columnDefinition = "NUMERIC DEFAULT 0 CHECK (balance >= 0)")
    @Min(value = 0, message = "Balance cannot be negative")
    BigDecimal balance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    Currency currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'CLOSED', 'FROZEN'))")
    AccountStatus status;

    @Column(name = "monthly_limit", columnDefinition = "NUMERIC DEFAULT 10000")
    BigDecimal monthlyLimit;

}
