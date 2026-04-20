package org.example.replayservice.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "account_snapshots")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AccountSnapshot {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.RANDOM)
    @Column(nullable = false, updatable = false, name = "account_snapshot_id")
    UUID accountSnapshotId;

    @Column(name = "account_id", nullable = false)
    UUID accountId;

    @Column(nullable = false, columnDefinition = "NUMERIC DEFAULT 0")
    BigDecimal balance;

    @Column(nullable = false)
    Integer partition;

    @Column(name="event_offset", nullable = false)
    Long eventOffset;

    @CreationTimestamp
    @Column(name = "snapshot_time", updatable = false)
    LocalDateTime snapshotTime;
}
