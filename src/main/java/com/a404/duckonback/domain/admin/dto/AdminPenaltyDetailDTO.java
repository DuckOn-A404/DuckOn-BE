package com.a404.duckonback.domain.admin.dto;

import com.a404.duckonback.common.enums.PenaltyType;
import com.a404.duckonback.common.enums.PenaltyStatus;
import com.a404.duckonback.domain.penalty.entity.Penalty;
import com.a404.duckonback.domain.user.entity.User;
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
public class AdminPenaltyDetailDTO {
    private Long penaltyId;
    private Long userId;
    private String nickname;
    private String reason;
    private PenaltyType penaltyType;
    private PenaltyStatus status;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    
    private String userLoginId;
    private String email;
    private int previousPenaltyCount;
    private int totalReportedCount;

    public static AdminPenaltyDetailDTO fromEntity(Penalty penalty, User user) {
        return AdminPenaltyDetailDTO.builder()
            .penaltyId(penalty.getPenaltyId())
            .userId(penalty.getUser().getId())
            .nickname(penalty.getUser().getNickname())
            .reason(penalty.getReason())
            .penaltyType(penalty.getPenaltyType())
            .status(penalty.getStatus())
            .startAt(penalty.getStartAt())
            .endAt(penalty.getEndAt())
            .userLoginId(user.getUserId())
            .email(user.getEmail())
            .previousPenaltyCount(user.getPenalties().size())
            .totalReportedCount(user.getReportsAsReported().size())
            .build();
    }
}
