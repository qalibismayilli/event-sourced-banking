package org.example.accountservice.outbox.model;

public enum OutboxEventStatus {
    PENDING, PROCESSING, COMPLETED, FAILED, CANCELLED
}
