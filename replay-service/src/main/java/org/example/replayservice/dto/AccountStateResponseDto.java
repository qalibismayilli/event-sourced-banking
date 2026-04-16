package org.example.replayservice.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccountStateResponseDto {

    UUID accountId;
    BigDecimal balance;
    boolean snapshotUsed;
    Long snapshotOffset;
    Integer eventsReplayed;
    LocalDateTime replayedAt;

}
