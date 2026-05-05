package org.example.accountservice.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "outbox_events")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OutboxEvent {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.RANDOM)
    UUID id;

    @Column(name = "aggregate_type")
    @Enumerated(EnumType.STRING)
    AggregateType aggregateType;

    @Column(name = "aggregate_id")
    UUID aggregateId;

    @Column(name = "event_type")
    @Enumerated(EnumType.STRING)
    EventType eventType;

    Object payload;

    @Enumerated(EnumType.STRING)
    OutboxEventStatus status;

    @Column(name = "created_date")
    LocalDateTime createdDate;
}
