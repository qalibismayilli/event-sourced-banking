package org.example.accountservice.outbox.repository;

import org.example.accountservice.outbox.model.OutboxEventStatus;
import org.example.accountservice.outbox.model.OutboxMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface OutboxRepository extends JpaRepository<OutboxMessage, UUID> {

    @Modifying
    @Query(value = """
    UPDATE outbox_messages 
    SET status = 'PROCESSING' 
    WHERE id IN (
        SELECT id FROM outbox_messages
        WHERE status = :status
        ORDER BY created_date ASC
        LIMIT 100
        FOR UPDATE SKIP LOCKED
    )
    RETURNING *
    """, nativeQuery = true)
    List<OutboxMessage> findAndMarkAsProcessing(@Param("status") OutboxEventStatus status);

    @Modifying
    @Query(value = """
    UPDATE outbox_messages
    SET status = :status
    WHERE id = :id
    """, nativeQuery = true
    )
    void updateStatus(@Param("id") UUID id ,@Param("status") OutboxEventStatus status);


    @Modifying
    @Query(value = """
    UPDATE outbox_messages
    SET retry_count = retry_count + 1, status = :status, error_message = :errorMessage 
    WHERE id = :id
    """, nativeQuery = true)
    void incrementRetryAndUpdateStatusAndSetErrorMessage(@Param("id") UUID id,
                                                         @Param("status") OutboxEventStatus status,
                                                         @Param("errorMessage") String errorMessage);
}
