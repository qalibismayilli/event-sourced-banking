package org.example.transactionservice.outbox.service;

import lombok.RequiredArgsConstructor;
import org.example.transactionservice.outbox.model.OutboxMessage;
import org.example.transactionservice.outbox.repository.OutboxRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OutboxService {
    private final OutboxRepository outboxRepository;

    public void save(OutboxMessage outboxMessage){
        outboxRepository.save(outboxMessage);
    }
}
