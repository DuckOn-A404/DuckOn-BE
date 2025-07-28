package com.a404.duckonback.dto;

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
