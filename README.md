# Event-Sourced Banking (Kafka-Based Demo)

## Overview

This project demonstrates core **Event Sourcing** concepts using Apache Kafka in a simplified banking domain.
The focus is on how state can be reconstructed from a sequence of events rather than being stored directly.

The system shows:

* Event-driven data flow
* Snapshot-based optimization
* Offset-based replay
* Deterministic state reconstruction

This is a **concept-focused implementation**. Production concerns such as idempotency, outbox pattern, and distributed transaction management are intentionally excluded and implemented in separate projects.

---

## Core Concept

Instead of persisting only the current state (e.g., account balance), the system stores a sequence of domain events:

* Deposit
* Withdraw
* Transfer

The current state is derived by replaying these events in order.

---

## Architecture

The system consists of the following components:

* **Transaction Service**
  Produces transaction events and publishes them to Kafka.

* **Account Service**
  Consumes events and updates account state (acts as a projection).

* **Replay Service**
  Reconstructs account state at a given point in time using snapshots and event replay.

* **Shared Events Module**
  Defines event contracts shared across services.

---

## Event Flow

1. A transaction is created in the Transaction Service
2. The event is published to Kafka
3. The Account Service consumes the event and updates the current state
4. The Replay Service can reconstruct historical state when needed

---

## Snapshot Strategy

To avoid replaying the entire event log:

* Snapshots are created periodically (based on event count)
* Each snapshot stores:

  * Account balance
  * Kafka partition
  * Offset

### Replay Algorithm

1. Retrieve the latest snapshot before the requested point in time
2. Seek to `snapshotOffset + 1`
3. Replay events until the defined boundary
4. Reconstruct the final state

---

## Replay Model

The system supports time-based state reconstruction by:

* Starting from a snapshot (if available)
* Replaying events in offset order
* Filtering events based on business time (`createdDate`)

Replay is bounded using Kafka offsets to ensure deterministic results.

---

## Design Decisions

### Partitioning

Kafka messages are produced with `accountId` as the key.
This ensures:

* All events for a given account are written to the same partition
* Ordering is preserved for replay

---

### Offset-Based Boundaries

Replay operations are bounded using offsets rather than timestamps.
This guarantees consistent and repeatable state reconstruction.

---

### Snapshot + Incremental Replay

Snapshots reduce the number of events that must be processed during replay.
Only events after the snapshot are applied.

---

## Trade-offs

The following concerns are intentionally excluded to keep the focus on event sourcing:

* Idempotent consumers
* Outbox pattern
* Distributed transaction guarantees
* Retry and dead-letter queue mechanisms

These topics are covered in separate projects.

---

## Purpose

This repository is intended to:

* Demonstrate practical event sourcing concepts
* Show how Kafka can be used as an event log
* Provide a clear and focused implementation of replay logic

