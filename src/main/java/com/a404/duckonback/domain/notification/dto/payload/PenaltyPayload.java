package com.a404.duckonback.domain.notification.dto.payload;

import com.a404.duckonback.common.enums.PenaltyStatus;
import com.a404.duckonback.common.enums.PenaltyType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PenaltyPayload implements NotificationPayload {
    private Long penaltyId;
    private PenaltyType penaltyType;
    private PenaltyStatus penaltyStatus;
    private String reason;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
}
