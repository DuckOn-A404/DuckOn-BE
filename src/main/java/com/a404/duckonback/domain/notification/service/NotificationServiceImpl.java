package com.a404.duckonback.domain.notification.service;

import com.a404.duckonback.common.dto.PageResponse;
import com.a404.duckonback.common.enums.RequestStatus;
import com.a404.duckonback.common.exception.CustomException;
import com.a404.duckonback.common.response.ErrorCode;
import com.a404.duckonback.domain.artist.common.ArtistSummaryDTO;
import com.a404.duckonback.domain.artist.request.entity.ArtistProfileChangeRequest;
import com.a404.duckonback.domain.artist.request.repository.ArtistChangeRequestRepository;
import com.a404.duckonback.domain.notification.dto.NotificationDetailDTO;
import com.a404.duckonback.domain.notification.dto.NotificationListDTO;
import com.a404.duckonback.domain.notification.dto.payload.ArtistChangeRequestPayload;
import com.a404.duckonback.domain.notification.dto.payload.EmptyPayload;
import com.a404.duckonback.domain.notification.dto.payload.NotificationPayload;
import com.a404.duckonback.domain.notification.dto.payload.PenaltyPayload;
import com.a404.duckonback.domain.notification.entity.Notification;
import com.a404.duckonback.domain.notification.entity.NotificationType;
import com.a404.duckonback.domain.notification.repository.NotificationRepository;
import com.a404.duckonback.domain.penalty.entity.Penalty;
import com.a404.duckonback.domain.penalty.repository.PenaltyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;
    private final ArtistChangeRequestRepository artistChangeRequestRepository;
    private final PenaltyRepository penaltyRepository;

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
                .body(request.getReviewComment() != null ? request.getReviewComment() : "")
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
                        .body(e.getBody())
                        .createdAt(e.getCreatedAt())
                        .readAt(e.getReadAt())
                        .sourceId(e.getSourceId())
                        .build()
        );
        return PageResponse.from1Base(dtoPage);
    }

    @Override
    @Transactional
    public void markAsRead(Long userId, Long notificationId) {
        // 1. 알림 존재 여부 확인
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOTIFICATION_NOT_FOUND));

        // 2. 알림 소유자 확인
        if(!notification.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        // 3. 읽음 처리
        if(notification.getReadAt() == null) {
            notification.markRead();
        }
    }

    @Override
    public NotificationDetailDTO<? extends NotificationPayload> getNotificationById(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOTIFICATION_NOT_FOUND));

        if(!notification.getUser().getId().equals(userId)) {
            log.warn(
                    "Notification ownership mismatch. notificationId={}, requestedUserId={}, notificationUserId={}",
                    notification.getId(), userId, notification.getUser().getId()
            );
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        NotificationPayload payload = mapPayload(notification);

        // 알림 상세 조회는 읽음 처리도 함께 수행
        if(notification.getReadAt() == null) {
            notification.markRead();
        }

        return NotificationDetailDTO.<NotificationPayload>builder()
                .id(notification.getId())
                .type(notification.getType())
                .title(notification.getTitle())
                .body(notification.getBody())
                .createdAt(notification.getCreatedAt())
                .readAt(notification.getReadAt())
                .sourceId(notification.getSourceId())
                .payload(payload)
                .build();
    }

    private NotificationPayload mapPayload(Notification notification) {
        return switch(notification.getType()){
            case ARTIST_CHANGE_REQUEST -> buildArtistChangeRequestPayload(notification);
            case PENALTY -> buildPenaltyPayload(notification);
            case SYSTEM_ALERT, REPORT_RESULT -> EmptyPayload.INSTANCE;
            default -> EmptyPayload.INSTANCE;
        };
    }

    private ArtistChangeRequestPayload buildArtistChangeRequestPayload(Notification notification) {
        ArtistProfileChangeRequest req = artistChangeRequestRepository.findById(notification.getSourceId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOTIFICATION_SOURCE_NOT_FOUND));

        // 1. 보안 체크 - 알림의 소유자와 아티스트 정보 변경 요청의 요청자가 일치하는지 확인
        if(!req.getRequestedBy().getId().equals(notification.getUser().getId())) {
            log.warn(
                    "Artist Change Request ownership mismatch. notificationId={}, ArtistProfileChangeRequestId={}, notificationUserId={}, ArtistProfileChangeRequesterUserId={}",
                    notification.getId(),
                    req.getId(),
                    notification.getUser().getId(),
                    req.getRequestedBy().getId()
            );
            throw  new CustomException(ErrorCode.NOTIFICATION_SOURCE_NOT_FOUND);
        }

        // 2. 타겟 아티스트 요약 만들기
        ArtistSummaryDTO artist = ArtistSummaryDTO.builder()
                .targetType(req.getTargetType())
                .id(req.getTargetId())
                .nameKr(req.getTargetNameKr())
                .nameEn(req.getTargetNameEn())
                .build();

        // 3. payload 조립
        return ArtistChangeRequestPayload.builder()
                .requestId(req.getId())
                .artist(artist)
                .content(req.getContent())
                .attachmentUrl(req.getAttachment())
                .requestStatus(req.getStatus())
                .reviewComment(req.getReviewComment())
                .reviewedAt(req.getReviewedAt())
                .requestCreatedAt(req.getCreatedAt())
                .requestUpdatedAt(req.getUpdatedAt())
                .build();
    }

    private PenaltyPayload buildPenaltyPayload(Notification notification) {
        Penalty p = penaltyRepository.findById(notification.getSourceId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOTIFICATION_SOURCE_NOT_FOUND));

        // 보안 체크 - 알림의 소유자와 패널티의 대상자가 일치하는지 확인
        if(!p.getUser().getId().equals(notification.getUser().getId())) {
            log.warn(
                    "Penalty ownership mismatch. notificationId={}, penaltyId={}, notificationUserId={}, penaltyUserId={}",
                    notification.getId(),
                    p.getPenaltyId(),
                    notification.getUser().getId(),
                    p.getUser().getId()
            );
            throw new CustomException(ErrorCode.NOTIFICATION_SOURCE_NOT_FOUND);
        }

        return PenaltyPayload.builder()
                .penaltyId(p.getPenaltyId())
                .penaltyType(p.getPenaltyType())
                .penaltyStatus(p.getStatus())
                .reason(p.getReason())
                .startAt(p.getStartAt())
                .endAt(p.getEndAt())
                .build();
    }
}
