package com.a404.duckonback.domain.notification.dto;

import com.a404.duckonback.domain.notification.dto.payload.NotificationPayload;
import com.a404.duckonback.domain.notification.entity.NotificationType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
public class NotificationDetailDTO<T extends NotificationPayload> {
    private Long id;
    private NotificationType type;
    private String title;
    private String body;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
    private Long sourceId;

    private T payload;
}