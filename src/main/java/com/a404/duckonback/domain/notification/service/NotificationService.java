package com.a404.duckonback.domain.notification.service;

import com.a404.duckonback.common.dto.PageResponse;
import com.a404.duckonback.domain.artist.request.entity.ArtistProfileChangeRequest;
import com.a404.duckonback.domain.notification.dto.NotificationDetailDTO;
import com.a404.duckonback.domain.notification.dto.NotificationListDTO;

public interface NotificationService {
    void notifyArtistChangeRequestReview(ArtistProfileChangeRequest request);
    PageResponse<NotificationListDTO> getMyNotifications(Long userId, int page, int size);
    NotificationDetailDTO getNotificationDetail(Long notificationId, Long userId);
}
