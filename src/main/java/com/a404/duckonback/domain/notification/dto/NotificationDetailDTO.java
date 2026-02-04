package com.a404.duckonback.domain.notification.dto;

import com.a404.duckonback.domain.notification.entity.NotificationSourceType;
import com.a404.duckonback.domain.notification.entity.NotificationType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDetailDTO {
    Long id;
    NotificationType type;
    String title;
    String body;
    String linkUrl;
    NotificationSourceType sourceType;
    Long sourceId;
    LocalDateTime readAt;
    LocalDateTime createdAt;
}
