# Event-Sourced Banking (Kafka-Based Demo)

## Overview

This project demonstrates **Event Sourcing** concepts using Apache Kafka in a simplified banking domain.
The focus is on how state can be reconstructed from a sequence of events rather than being stored directly.

The system shows:

- Event-driven data flow
- Transactional Outbox Pattern with retry mechanism
- Idempotent event publishing
- Snapshot-based optimization
- Offset-based replay
- Deterministic state reconstruction
- Service discovery with Consul
- API routing with Spring Cloud Gateway

---

## Architecture

The system consists of the following services:

- **Transaction Service** — Accepts deposit, withdraw, and transfer requests. Persists transactions and publishes events via the Outbox pattern.
- **Account Service** — Consumes transaction events and updates account state (acts as a projection). Publishes account lifecycle events (created, closed, frozen) via the Outbox pattern.
- **Replay Service** — Reconstructs account state at any given point in time using snapshots and incremental event replay.
- **Notification Service** — Consumes all domain events and simulates notification delivery (e.g. logging).
- **Gateway** — Single entry point. Routes requests to the appropriate service via Consul-based load balancing.
- **Shared Events Module** — Defines event contracts shared across all services.

---

## Infrastructure

| Component | Details                              |
|-----------|--------------------------------------|
| Kafka     | 3-node KRaft cluster (no ZooKeeper)  |
| Database  | PostgreSQL (separate DB per service) |
| Discovery | Consul                               |
| Gateway   | Spring Cloud Gateway                 |

Kafka is configured with:
- `acks=all`
- `min.insync.replicas=2`
- `enable.idempotence=true`

---

## Core Concept

Instead of persisting only the current state (e.g. account balance), the system stores a sequence of domain events:

- Deposit
- Withdraw
- Transfer

The current state is derived by replaying these events in order.

---

## Event Flow

1. A transaction request arrives at the Transaction Service
2. The transaction is persisted to the database
3. An `OutboxMessage` is saved in the same transaction (atomicity guaranteed)
4. The Outbox scheduler picks up pending messages and publishes them to Kafka
5. The Account Service consumes the event and updates the account balance
6. The Account Service publishes account lifecycle events (e.g. `AccountCreatedEvent`) via its own Outbox
7. The Notification Service consumes all domain events

---

## Outbox Pattern

Both the Transaction Service and Account Service implement the Transactional Outbox pattern to guarantee at-least-once delivery.

**Flow:**
```
DB transaction:
  save entity  +  save OutboxMessage (PENDING)

Scheduler (every 5s):
  fetch PENDING messages (FOR UPDATE SKIP LOCKED)
  publish to Kafka
  mark as COMPLETED

On failure:
  retry up to 3 times → mark as FAILED

Scheduler (every 10min):
  retry FAILED messages up to 10 times → mark as CANCELLED
```

`FOR UPDATE SKIP LOCKED` ensures safe concurrent processing across multiple service instances.

---

## Snapshot Strategy

To avoid replaying the entire event log from the beginning:

- Snapshots are taken every **100 events** per account
- Each snapshot stores:
  - Account balance
  - Kafka partition
  - Kafka offset
  - Snapshot timestamp

### Replay Algorithm

1. Find the latest snapshot before the requested timestamp
2. Seek to `snapshotOffset + 1`
3. Replay events up to the requested timestamp
4. Return the reconstructed state

---

## Design Decisions

### Partitioning

Kafka messages are produced with `accountId` as the key. This ensures:
- All events for a given account land in the same partition
- Ordering is preserved during replay

### Offset-Based Boundaries

Replay is bounded using Kafka offsets rather than timestamps. This guarantees consistent and repeatable state reconstruction regardless of consumer timing.

### Idempotent Producer

All Kafka producers are configured with `enable.idempotence=true` and `acks=all`, preventing duplicate messages in case of retries.

---

## Trade-offs & Intentional Simplifications

The following concerns are simplified to keep the focus on event sourcing:

- **Idempotent consumers** — Not implemented. Duplicate event consumption could lead to incorrect balance updates.
- **Distributed transaction guarantees** — No saga pattern. The Transaction Service marks all transactions as `SUCCESS` without confirming the Account Service result.
- **Dead-letter queue** — After max retries, messages are marked `CANCELLED` in the outbox but not forwarded to a DLQ topic.

These topics are addressed in separate projects.

---

## Purpose

This repository is intended to:

- Demonstrate practical event sourcing with Kafka
- Show the Transactional Outbox pattern in a microservices context
- Provide a clear implementation of snapshot-based replay logic
