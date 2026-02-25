package com.a404.duckonback.domain.notification.dto.payload;

import com.a404.duckonback.common.enums.RequestStatus;
import com.a404.duckonback.domain.artist.common.ArtistSummaryDTO;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ArtistChangeRequestPayload implements NotificationPayload {
    private Long requestId;

    private ArtistSummaryDTO artist;
    private String content;
    private String attachmentUrl;

    private RequestStatus requestStatus;
    private String reviewComment;
    private LocalDateTime reviewedAt;

    private LocalDateTime requestCreatedAt;
    private LocalDateTime requestUpdatedAt;
}
