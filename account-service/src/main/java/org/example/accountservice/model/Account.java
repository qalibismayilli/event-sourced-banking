package org.example.accountservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.example.sharedevents.util.AccountStatus;
import org.example.sharedevents.util.Currency;
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

    @Column(nullable = false)
    @Min(value = 0, message = "Balance cannot be negative")
    BigDecimal balance = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    Currency currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    AccountStatus status = AccountStatus.ACTIVE;

    @Column(name = "monthly_limit")
    BigDecimal monthlyLimit = BigDecimal.valueOf(10000);

}
