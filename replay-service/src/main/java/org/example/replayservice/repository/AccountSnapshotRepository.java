package org.example.replayservice.repository;

import org.example.replayservice.model.AccountSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface AccountSnapshotRepository extends JpaRepository<AccountSnapshot, UUID> {
    Optional<AccountSnapshot> findByAccountId(UUID accountId);


    Optional<AccountSnapshot> findTopByAccountIdAndSnapshotTimeBeforeOrderBySnapshotTimeDesc(UUID accountId, LocalDateTime snapshotTime);

}
