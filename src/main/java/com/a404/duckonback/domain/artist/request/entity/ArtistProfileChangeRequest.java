package com.a404.duckonback.domain.artist.request.entity;

import com.a404.duckonback.common.enums.RequestStatus;
import com.a404.duckonback.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "artist_profile_change_request",
        indexes = {
                @Index(name = "idx_apcr_status_created_at", columnList = "status, created_at"),
                @Index(name = "idx_apcr_target", columnList = "target_type, target_id, status"),
                @Index(name = "idx_apcr_requested_by", columnList = "requested_by_user_id, created_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArtistProfileChangeRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 30)
    private ArtistChangeTargetType targetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_by_user_id", nullable = false)
    private User requestedBy;

    /**
     * 검토자(어드민). 유저 요청 생성 단계에서는 null.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by_user_id")
    private User reviewedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private RequestStatus status;

    /**
     * 유저가 입력하는 변경 요청 내용 (텍스트)
     */
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * 첨부 1개 (이미지 URL / 링크 / 텍스트 등)
     */
    @Column(name = "attachment", columnDefinition = "TEXT")
    private String attachment;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (this.createdAt == null) this.createdAt = now;
        this.updatedAt = now;
        if (this.status == null) this.status = RequestStatus.PENDING;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void cancelByRequester() {
        this.status = RequestStatus.CANCELED;
    }
}
