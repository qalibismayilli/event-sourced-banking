package org.example.replayservice.service;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.example.replayservice.dto.AccountStateResponseDto;
import org.example.replayservice.model.AccountSnapshot;
import org.example.replayservice.repository.AccountSnapshotRepository;
import org.example.sharedevents.event.TransactionExecutedEvent;
import org.springframework.core.env.Environment;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReplayService {

    private final AccountSnapshotRepository snapshotRepository;
    private final Environment env;

    private static final String TOPIC = "transaction-executed-events-topic";
    private static final int SNAPSHOT_INTERVAL = 100;


    private KafkaConsumer<String, TransactionExecutedEvent> createConsumer() {
        Properties props = new Properties();
        props.put("bootstrap.servers", env.getProperty("spring.kafka.consumer.bootstrap-serversCustomer"));
        props.put("group.id", "replay-service-" + UUID.randomUUID());
        props.put("key.deserializer", StringDeserializer.class);
        props.put("value.deserializer", JacksonJsonDeserializer.class);
        return new KafkaConsumer<>(props);
    }

    public void processEvent(TransactionExecutedEvent event, int partition, long offset) {
        snapshotRepository.findByAccountId((event.getAccountId())).ifPresentOrElse(
                snapshot -> {
                    if (offset - snapshot.getEventOffset() >= SNAPSHOT_INTERVAL) {
                        takeSnapshot(event.getAccountId(), calculateBalance(event.getAccountId(), snapshot, offset), partition, offset);
                    }
                },
                () -> takeSnapshot(event.getAccountId(), event.getAmount(), partition, offset)
        );
    }

    public AccountStateResponseDto replayAt(UUID accountId, LocalDateTime date) {

        try(KafkaConsumer<String, TransactionExecutedEvent> consumer = createConsumer()) {
            Optional<AccountSnapshot> snapshot = snapshotRepository
                    .findTopByAccountIdAndSnapshotTimeBeforeOrderBySnapshotTimeDesc(accountId, date);

            TopicPartition topicPartition;
            long startOffset;

            if (snapshot.isPresent()) {
                topicPartition = new TopicPartition(TOPIC, snapshot.get().getPartition());
                startOffset = snapshot.get().getEventOffset()+1;
            } else {
                topicPartition = new TopicPartition(TOPIC, 0);
                startOffset = 0;
            }

            consumer.assign(List.of(topicPartition));
            consumer.seek(topicPartition, startOffset);

            BigDecimal balance = snapshot.map(AccountSnapshot::getBalance).orElse(BigDecimal.ZERO);
            int eventsReplayed = 0;

            while (true) {
                ConsumerRecords<String, TransactionExecutedEvent> records = consumer.poll(Duration.ofMillis(500));
                if (records.isEmpty()) break;

                for (ConsumerRecord<String, TransactionExecutedEvent> record : records) {
                    TransactionExecutedEvent event = record.value();

                    if (!event.getAccountId().equals(accountId)) continue;
                    if (event.getCreatedDate().isAfter(date)) continue ;

                    balance = applyEvent(balance, event, accountId);
                    eventsReplayed++;
                }
            }

            return AccountStateResponseDto.builder()
                    .accountId(accountId)
                    .balance(balance)
                    .snapshotUsed(snapshot.isPresent())
                    .snapshotOffset(snapshot.map(AccountSnapshot::getEventOffset).orElse(0L))
                    .eventsReplayed(eventsReplayed)
                    .replayedAt(date)
                    .build();
        }
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
                .eventOffset(offset)
                .snapshotTime(LocalDateTime.now())
                .build();
        snapshotRepository.save(snapshot);
    }

    private BigDecimal calculateBalance(UUID accountId, AccountSnapshot snapshot, long currentOffset) {

        try (KafkaConsumer<String, TransactionExecutedEvent> consumer = createConsumer()) {
            TopicPartition topicPartition = new TopicPartition(TOPIC, snapshot.getPartition());
            consumer.assign(List.of(topicPartition));
            consumer.seek(topicPartition, snapshot.getEventOffset()+1);
            BigDecimal balance = snapshot.getBalance();

            outer:
            while (true) {
                ConsumerRecords<String, TransactionExecutedEvent> records = consumer.poll(Duration.ofMillis(500));
                if (records.isEmpty()) break;

                for (ConsumerRecord<String, TransactionExecutedEvent> record : records) {
                    if (record.offset() >= currentOffset) break outer;

                    TransactionExecutedEvent event = record.value();
                    if (event.getAccountId().equals(accountId)) {
                        balance = applyEvent(balance, event, accountId);
                    }
                }
            }
            return balance;
        }
    }
}