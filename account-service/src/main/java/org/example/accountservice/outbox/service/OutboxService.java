package org.example.accountservice.outbox.service;

import lombok.RequiredArgsConstructor;
import org.example.accountservice.outbox.model.OutboxMessage;
import org.example.accountservice.outbox.repository.OutboxRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OutboxService {
    private final OutboxRepository outboxRepository;

    public void save(OutboxMessage outboxMessage){
        outboxRepository.save(outboxMessage);
    }
}
