package org.example.accountservice.outbox.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "outbox_messages")
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OutboxMessage {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.RANDOM)
    UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "aggregate_type")
    AggregateType aggregateType;

    @Column(name = "aggregate_id")
    UUID aggregateId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type")
    EventType eventType;

    @Column(name = "payload", columnDefinition = "jsonb")
    String payload;

    @Enumerated(EnumType.STRING)
    OutboxEventStatus status;

    @Column(name = "retry_count")
    int retryCount;

    @Column(name = "error_message")
    String errorMessage;

    @Column(name = "created_date")
    LocalDateTime createdDate;

    @Column(name = "processed_date")
    LocalDateTime processedDate;
}
