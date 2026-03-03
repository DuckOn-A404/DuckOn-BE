package com.a404.duckonback.domain.admin.dto;

import com.a404.duckonback.common.enums.UserRole;
import com.a404.duckonback.common.enums.SocialProvider;
import com.a404.duckonback.domain.user.entity.User;

import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminUserDetailDTO {
    // 기본 정보
    private Long id;
    private String userId;
    private String email;
    private String nickname;
    private String imgUrl;
    
    // 계정 상태
    private UserRole role;
    private boolean deleted;
    private LocalDateTime deletedAt;
    
    // 가입 정보
    private SocialProvider provider;
    private LocalDateTime createdAt;
    private Instant lastLoginAt;
    
    // 활동 통계 (Count)
    private int reportedCount;      // 신고당한 횟수
    private int reporterCount;      // 신고한 횟수
    private int penaltyCount;       // 제재 횟수
    private int blockedByCount;     // 차단당한 횟수
    private int roomCount;          // 생성한 방 수
    private int memeCount;          // 생성한 밈 수

    public static AdminUserDetailDTO fromEntity(User user) {
        return AdminUserDetailDTO.builder()
                .id(user.getId())
                .userId(user.getUserId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .imgUrl(user.getImgUrl())
                .role(user.getRole())
                .deleted(user.isDeleted())
                .deletedAt(user.getDeletedAt())
                .provider(user.getProvider())
                .createdAt(user.getCreatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .reportedCount(user.getReportsAsReported().size())
                .reporterCount(user.getReportsAsReporter().size())
                .penaltyCount(user.getPenalties().size())
                .blockedByCount(user.getBlockedByUsers().size())
                .roomCount(user.getRooms().size())
                .memeCount(user.getMemes().size())
                .build();
    }
}
