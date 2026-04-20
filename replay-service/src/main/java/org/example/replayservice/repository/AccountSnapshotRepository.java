package org.example.replayservice.repository;

import org.example.replayservice.model.AccountSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface AccountSnapshotRepository extends JpaRepository<AccountSnapshot, UUID> {
    Optional<AccountSnapshot> findByAccountId(UUID accountId);


    @Query(value = """
            select *from account_snapshots
            where account_id = :accountId and snapshot_time <= :snapshotTime
            order by snapshot_time desc
            limit 1
            """,
            nativeQuery = true)
    Optional<AccountSnapshot> findTopByAccountIdAndSnapshotTimeBeforeOrderBySnapshotTimeDesc(
            @Param("accountId") UUID accountId,
            @Param("snapshotTime") LocalDateTime snapshotTime
    );

}
