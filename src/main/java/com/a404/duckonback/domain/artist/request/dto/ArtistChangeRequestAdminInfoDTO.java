package com.a404.duckonback.domain.artist.request.dto;

import com.a404.duckonback.common.enums.RequestStatus;
import com.a404.duckonback.domain.artist.request.entity.ArtistChangeTargetType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ArtistChangeRequestAdminInfoDTO {

    private Long id;

    private ArtistChangeTargetType targetType;
    private Long targetId;
    private String artistNameEn;;
    private String artistNameKr;

    private String content;
    private String attachment;

    private RequestStatus status;

    private Long requestedByUserId;

    private LocalDateTime requestedAt;
}
