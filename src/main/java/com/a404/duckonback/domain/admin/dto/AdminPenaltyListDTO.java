package com.a404.duckonback.domain.admin.dto;

import com.a404.duckonback.common.enums.PenaltyType;
import com.a404.duckonback.common.enums.PenaltyStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminPenaltyListDTO {

    private Long penaltyId;
    private Long userId;
    private String nickname;
    private String reason;
    private PenaltyType penaltyType;
    private PenaltyStatus status;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
}
