package org.example.replayservice.service;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.example.replayservice.dto.AccountStateResponseDto;
import org.example.replayservice.model.AccountSnapshot;
import org.example.replayservice.repository.AccountSnapshotRepository;
import org.example.sharedevents.event.TransactionExecutedEvent;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReplayService {

    private final AccountSnapshotRepository snapshotRepository;
    private final KafkaConsumer<String, TransactionExecutedEvent> kafkaConsumer;

    private static final String TOPIC = "transaction-executed-events-topic";
    private static final int SNAPSHOT_INTERVAL = 100;

    public void processEvent(TransactionExecutedEvent event, int partition, long offset) {
        snapshotRepository.findByAccountId((event.getAccountId())).ifPresentOrElse(
                snapshot -> {
                    if (offset - snapshot.getOffset() >= SNAPSHOT_INTERVAL) {
                        takeSnapshot(event.getAccountId(), calculateBalance(event.getAccountId(), snapshot, offset), partition, offset);
                    }
                },
                () -> takeSnapshot(event.getAccountId(), event.getAmount(), partition, offset)
        );
    }

    public AccountStateResponseDto replayAt(UUID accountId, LocalDateTime date) {
        Optional<AccountSnapshot> snapshot = snapshotRepository
                .findTopByAccountIdAndSnapshotTimeBeforeOrderBySnapshotTimeDesc(accountId, date);

        TopicPartition topicPartition;
        long startOffset;

        if (snapshot.isPresent()) {
            topicPartition = new TopicPartition(TOPIC, snapshot.get().getPartition());
            startOffset = snapshot.get().getOffset();
        } else {
            topicPartition = new TopicPartition(TOPIC, 0);
            startOffset = 0;
        }

        kafkaConsumer.assign(List.of(topicPartition));
        kafkaConsumer.seek(topicPartition, startOffset);

        // 3. Eventləri oxu, balansı hesabla
        BigDecimal balance = snapshot.map(AccountSnapshot::getBalance).orElse(BigDecimal.ZERO);
        int eventsReplayed = 0;

        while (true) {
            ConsumerRecords<String, TransactionExecutedEvent> records = kafkaConsumer.poll(Duration.ofMillis(500));
            if (records.isEmpty()) break;

            for (ConsumerRecord<String, TransactionExecutedEvent> record : records) {
                TransactionExecutedEvent event = record.value();

                if (!event.getAccountId().equals(accountId)) continue;
                if (event.getCreatedDate().isAfter(date)) break;

                balance = applyEvent(balance, event, accountId);
                eventsReplayed++;
            }
        }

        return AccountStateResponseDto.builder()
                .accountId(accountId)
                .balance(balance)
                .snapshotUsed(snapshot.isPresent())
                .snapshotOffset(snapshot.map(AccountSnapshot::getOffset).orElse(0L))
                .eventsReplayed(eventsReplayed)
                .replayedAt(date)
                .build();
    }

    private BigDecimal applyEvent(BigDecimal balance, TransactionExecutedEvent event, UUID accountId) {
        switch (event.getType()) {
            case DEPOSIT:
                return balance.add(event.getAmount());
            case WITHDRAW:
                return balance.subtract(event.getAmount());
            case TRANSFER:
                if (event.getAccountId().equals(accountId)) {
                    return balance.subtract(event.getAmount());
                } else {
                    return balance.add(event.getAmount());
                }
            default:
                return balance;
        }
    }

    private void takeSnapshot(UUID accountId, BigDecimal balance, int partition, long offset) {
        AccountSnapshot snapshot = AccountSnapshot.builder()
                .accountId(accountId)
                .balance(balance)
                .partition(partition)
                .offset(offset)
                .snapshotTime(LocalDateTime.now())
                .build();
        snapshotRepository.save(snapshot);
    }

    private BigDecimal calculateBalance(UUID accountId, AccountSnapshot snapshot, long currentOffset) {
        TopicPartition topicPartition = new TopicPartition(TOPIC, snapshot.getPartition());
        kafkaConsumer.assign(List.of(topicPartition));
        kafkaConsumer.seek(topicPartition, snapshot.getOffset());
        BigDecimal balance = snapshot.getBalance();

        while (true) {
            ConsumerRecords<String, TransactionExecutedEvent> records = kafkaConsumer.poll(Duration.ofMillis(500));
            if (records.isEmpty()) break;

            for (ConsumerRecord<String, TransactionExecutedEvent> record : records) {
                if (record.offset() >= currentOffset) break;

                TransactionExecutedEvent event = record.value();
                if (event.getAccountId().equals(accountId)) {
                    balance = applyEvent(balance, event, accountId);
                }
            }
        }
        return balance;
    }
}