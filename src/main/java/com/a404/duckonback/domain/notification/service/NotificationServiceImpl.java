package com.a404.duckonback.domain.notification.service;

import com.a404.duckonback.common.dto.PageResponse;
import com.a404.duckonback.common.enums.RequestStatus;
import com.a404.duckonback.common.exception.CustomException;
import com.a404.duckonback.common.response.ErrorCode;
import com.a404.duckonback.domain.artist.request.entity.ArtistProfileChangeRequest;
import com.a404.duckonback.domain.notification.dto.NotificationDetailDTO;
import com.a404.duckonback.domain.notification.dto.NotificationListDTO;
import com.a404.duckonback.domain.notification.entity.Notification;
import com.a404.duckonback.domain.notification.entity.NotificationSourceType;
import com.a404.duckonback.domain.notification.entity.NotificationType;
import com.a404.duckonback.domain.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;

    @Override
    public void notifyArtistChangeRequestReview(ArtistProfileChangeRequest request) {
        if(request.getStatus() != RequestStatus.APPROVED && request.getStatus() != RequestStatus.REJECTED) {
            return;
        }

        String artistName = request.getTargetNameKr() != null && !request.getTargetNameKr().isBlank()
                ? request.getTargetNameKr()
                : request.getTargetNameEn();

        String title = "[아티스트 정보 수정 요청] " + artistName + " 정보 수정 요청 처리 결과 안내";

        Notification notification = Notification.builder()
                .user(request.getRequestedBy())
                .type(NotificationType.ARTIST_CHANGE_REQUEST)
                .title(title)
                .body(request.getReviewComment())
                .linkUrl("/me/artist-change-requests/" + request.getId())
                .sourceType(NotificationSourceType.ARTIST_PROFILE_CHANGE_REQUEST) // TODO : 프론트 라우팅 규칙 확인 후 수정 필요
                .sourceId(request.getId())
                .build();

        notificationRepository.save(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<NotificationListDTO> getMyNotifications(Long userId, int page, int size) {
        int safePage = Math.max(page - 1, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);

        Pageable pageable = PageRequest.of(safePage, safeSize);
        Page<Notification> pageResult = notificationRepository.findByUser_IdOrderByCreatedAtDesc(userId, pageable);

        Page<NotificationListDTO> dtoPage = pageResult.map(e ->
                NotificationListDTO.builder()
                        .id(e.getId())
                        .type(e.getType())
                        .title(e.getTitle())
                        .createdAt(e.getCreatedAt())
                        .readAt(e.getReadAt())
                        .linkUrl(e.getLinkUrl())
                        .build()
        );
        return PageResponse.from1Base(dtoPage);
    }

    @Override
    public NotificationDetailDTO getNotificationDetail(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOTIFICATION_NOT_FOUND));

        if(!notification.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        if(notification.getReadAt() == null) {
            notification.markRead();
        }

        return NotificationDetailDTO.builder()
                .id(notification.getId())
                .type(notification.getType())
                .sourceType(notification.getSourceType())
                .sourceId(notification.getSourceId())
                .title(notification.getTitle())
                .body(notification.getBody())
                .createdAt(notification.getCreatedAt())
                .readAt(notification.getReadAt())
                .linkUrl(notification.getLinkUrl())
                .build();
    }
}
