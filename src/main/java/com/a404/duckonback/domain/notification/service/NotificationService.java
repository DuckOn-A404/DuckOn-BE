package com.a404.duckonback.domain.notification.service;

import com.a404.duckonback.common.dto.PageResponse;
import com.a404.duckonback.domain.artist.request.entity.ArtistProfileChangeRequest;
import com.a404.duckonback.domain.notification.dto.NotificationDetailDTO;
import com.a404.duckonback.domain.notification.dto.NotificationListDTO;
import com.a404.duckonback.domain.notification.dto.payload.NotificationPayload;

public interface NotificationService {
    void notifyArtistChangeRequestReview(ArtistProfileChangeRequest request);
    PageResponse<NotificationListDTO> getMyNotifications(Long userId, int page, int size);
    void markAsRead(Long userId, Long notificationId);
    NotificationDetailDTO<? extends NotificationPayload> getNotificationById(Long userId, Long notificationId);
}
