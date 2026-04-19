package org.example.replayservice.hadler;

import lombok.RequiredArgsConstructor;
import org.example.replayservice.service.ReplayService;
import org.example.sharedevents.event.TransactionExecutedEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransactionExecutedEventHandler {
    private final ReplayService replayService;

    @KafkaListener(topics = "transaction-executed-events-topic")
    public void handleTransactionExecuted(
            @Payload TransactionExecutedEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset
            ) {
        replayService.processEvent(event, partition, offset);
    }
}
