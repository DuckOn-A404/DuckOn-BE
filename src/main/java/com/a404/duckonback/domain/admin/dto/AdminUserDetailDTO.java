package com.a404.duckonback.domain.admin.dto;

import com.a404.duckonback.common.enums.PenaltyType;
import com.a404.duckonback.common.enums.ReportStatus;
import com.a404.duckonback.common.enums.ReportType;
import com.a404.duckonback.common.enums.UserRole;
import com.a404.duckonback.common.enums.SocialProvider;
import com.a404.duckonback.domain.penalty.entity.Penalty;
import com.a404.duckonback.domain.report.entity.Report;
import com.a404.duckonback.domain.user.entity.User;

import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

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

    // JOIN으로 가져온 상세 데이터
    private List<ActivePenaltyDTO> activePenalties;         // 현재 활성 제재 목록
    private List<RecentReportDTO> recentReportsAsReported;  // 최근 신고당한 내역
    private List<String> followingArtists;                  // 팔로우 중인 아티스트

    // 현재 활성 제재 DTO
    @Getter
    @Builder
    public static class ActivePenaltyDTO {
        private PenaltyType type;
        private String reason;
        private LocalDateTime startAt;
        private LocalDateTime endAt;

        public static ActivePenaltyDTO from(Penalty penalty) {
            return ActivePenaltyDTO.builder()
                    .type(penalty.getPenaltyType())
                    .reason(penalty.getReason())
                    .startAt(penalty.getStartAt())
                    .endAt(penalty.getEndAt())
                    .build();
        }
    }

    // 최근 신고당한 내역 DTO
    @Getter
    @Builder
    public static class RecentReportDTO {
        private ReportType type;
        private ReportStatus status;
        private String reason;
        private LocalDateTime reportedAt;
        private String reporterUserId;

        public static RecentReportDTO from(Report report) {
            return RecentReportDTO.builder()
                    .type(report.getReportType())
                    .status(report.getReportStatus())
                    .reason(report.getReportReason())
                    .reportedAt(report.getReportedAt())
                    .reporterUserId(report.getReporter().getUserId())
                    .build();
        }
    }

    // public static AdminUserDetailDTO fromEntity(User user) {
    //     return AdminUserDetailDTO.builder()
    //             .id(user.getId())
    //             .userId(user.getUserId())
    //             .email(user.getEmail())
    //             .nickname(user.getNickname())
    //             .imgUrl(user.getImgUrl())
    //             .role(user.getRole())
    //             .deleted(user.isDeleted())
    //             .deletedAt(user.getDeletedAt())
    //             .provider(user.getProvider())
    //             .createdAt(user.getCreatedAt())
    //             .lastLoginAt(user.getLastLoginAt())
    //             .reportedCount(user.getReportsAsReported().size())
    //             .reporterCount(user.getReportsAsReporter().size())
    //             .penaltyCount(user.getPenalties().size())
    //             .blockedByCount(user.getBlockedByUsers().size())
    //             .roomCount(user.getRooms().size())
    //             .memeCount(user.getMemes().size())
    //             .build();
    // }

    // Service에서 추가 데이터를 설정할 때 사용
    public static AdminUserDetailDTO fromEntity(
            User user,
            List<Penalty> activePenalties,
            List<Report> recentReports,
            List<String> followingArtists
    ) {
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
                .activePenalties(activePenalties.stream().map(ActivePenaltyDTO::from).toList())
                .recentReportsAsReported(recentReports.stream().map(RecentReportDTO::from).toList())
                .followingArtists(followingArtists)
                .build();
    }
}
