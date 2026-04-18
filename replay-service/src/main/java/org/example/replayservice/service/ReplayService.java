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
import tools.jackson.databind.ObjectMapper;

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
    private final KafkaConsumer<String, Object> kafkaConsumer;
    private final ObjectMapper objectMapper;

    private static final String TOPIC = "transaction-executed-events-topic";
    private static final int SNAPSHOT_INTERVAL = 100;

    // Kafka-dan event gəlir, snapshot yoxlanılır
    public void processEvent(org.example.sharedevents.event.TransactionExecutedEvent event, int partition, long offset) {
        // Snapshot lazımdırmı yoxla
        snapshotRepository.findByAccountId((event.getAccountId())).ifPresentOrElse(
                snapshot -> {
                    if (offset - snapshot.getOffset() >= SNAPSHOT_INTERVAL) {
                        takeSnapshot(event.getAccountId(), calculateBalance(event.getAccountId(), snapshot, offset), partition, offset);
                    }
                },
                () -> takeSnapshot(event.getAccountId(), event.getAmount(), partition, offset)
        );
    }

    // Time-travel API
    public AccountStateResponseDto replayAt(UUID accountId, LocalDateTime at) {
        // 1. Ən yaxın snapshot-u tap
        Optional<AccountSnapshot> snapshot = snapshotRepository
                .findTopByAccountIdAndSnapshotTimeBeforeOrderBySnapshotTimeDesc(accountId, at);

        // 2. Kafka-dan replay et
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
            ConsumerRecords<String, Object> records = kafkaConsumer.poll(Duration.ofMillis(500));
            if (records.isEmpty()) break;

            for (ConsumerRecord<String, Object> record : records) {
                TransactionExecutedEvent event = (TransactionExecutedEvent) record.value();

                if (!event.getAccountId().equals(accountId)) continue;
                if (event.getCreatedDate().isAfter(at)) break;

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
                .replayedAt(at)
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
        // Snapshot-dan bu ana qədər olan eventləri replay et
        TopicPartition topicPartition = new TopicPartition(TOPIC, snapshot.getPartition());
        kafkaConsumer.assign(List.of(topicPartition));
        kafkaConsumer.seek(topicPartition, snapshot.getOffset());

        BigDecimal balance = snapshot.getBalance();

        while (true) {
            ConsumerRecords<String, Object> records = kafkaConsumer.poll(Duration.ofMillis(500));
            if (records.isEmpty()) break;

            for (ConsumerRecord<String, Object> record : records) {
                if (record.offset() >= currentOffset) break;

                TransactionExecutedEvent event = (TransactionExecutedEvent) record.value();
                if (event.getAccountId().equals(accountId)) {
                    balance = applyEvent(balance, event, accountId);
                }
            }
        }
        return balance;
    }
}