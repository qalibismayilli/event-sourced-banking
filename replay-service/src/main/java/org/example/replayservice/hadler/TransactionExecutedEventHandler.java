package org.example.replayservice.hadler;

import lombok.RequiredArgsConstructor;
import org.example.replayservice.service.ReplayService;
import org.example.sharedevents.event.TransactionExecutedEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class TransactionExecutedEventHandler {
    private final ObjectMapper objectMapper;
    private final ReplayService replayService;

    @KafkaListener(topics = "transaction-executed-events-topic")
    public void handleTransactionExecuted(String message) {

         TransactionExecutedEvent event = objectMapper.readValue(message, TransactionExecutedEvent.class);
//        replayService.processEvent(event, );
    }
}
