package org.example.replayservice.repository;

import org.example.replayservice.model.AccountSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface AccountSnapshotRepository extends JpaRepository<AccountSnapshot, UUID> {
    Optional<AccountSnapshot> findByAccountId(UUID accountId);


    @Query(value = """
            select * from 
            """,
            nativeQuery = true)
    Optional<AccountSnapshot> findTopByAccountIdAndSnapshotTimeBeforeOrderBySnapshotTimeDesc(
            UUID accountId,
            LocalDateTime snapshotTime
    );

}
