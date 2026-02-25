package com.a404.duckonback.domain.notification.entity;

import com.a404.duckonback.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "notification",
        indexes = {
                @Index(name = "idx_notification_user_created_at", columnList = "user_id, created_at"),
                @Index(name = "idx_notification_user_read_at", columnList = "user_id, read_at"),
                @Index(name = "idx_notification_source", columnList = "type, source_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 알림 수신자
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 알림 카테고리 (프론트 표시/필터링 용)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private NotificationType type;

    /**
     * 알림 제목/요약 (리스트에 보여주기 좋게)
     */
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    /**
     * 알림 본문 (상세/팝업에 쓰기)
     */
    @Column(name = "body", nullable = false, columnDefinition = "TEXT")
    private String body;

    @Column(name = "source_id", nullable = false)
    private Long sourceId;

    /**
     * 읽음 처리 시간 (null이면 unread)
     */
    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
    }

    // ---- 편의 메서드 ----
    public boolean isRead() {
        return readAt != null;
    }

    public void markRead() {
        this.readAt = LocalDateTime.now();
    }
}
