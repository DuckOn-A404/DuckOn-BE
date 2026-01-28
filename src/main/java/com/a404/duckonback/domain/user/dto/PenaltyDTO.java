package com.a404.duckonback.domain.user.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PenaltyDTO {
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private String reason;
    private String status;
    private String penaltyType;
}
