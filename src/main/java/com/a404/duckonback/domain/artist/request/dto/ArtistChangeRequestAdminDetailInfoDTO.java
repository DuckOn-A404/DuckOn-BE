package com.a404.duckonback.domain.artist.request.dto;

import com.a404.duckonback.domain.admin.dto.UserSummaryDTO;
import com.a404.duckonback.domain.artist.request.entity.ArtistChangeTargetType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ArtistChangeRequestAdminDetailInfoDTO {
    private Long id;

    private ArtistChangeTargetType targetType;
    private Long targetId;
    private String artistNameEn;;
    private String artistNameKr;

    private String content;
    private String attachment;

    private String status;

    private UserSummaryDTO requester;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private UserSummaryDTO reviewedBy;
    private String reviewComment;
    private LocalDateTime reviewedAt;

}
