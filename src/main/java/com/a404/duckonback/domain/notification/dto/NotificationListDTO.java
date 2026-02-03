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
public class NotificationListDTO {
    private Long id;
    private NotificationType type;
    private String title;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
    private String linkUrl;

}
