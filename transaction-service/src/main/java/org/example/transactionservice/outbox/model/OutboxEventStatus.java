package org.example.transactionservice.outbox.model;

public enum OutboxEventStatus {
    PENDING, PROCESSING, COMPLETED, FAILED, CANCELLED
}
