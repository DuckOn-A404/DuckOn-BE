package com.a404.duckonback.entity;

import com.a404.duckonback.enums.MemeUsageType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "meme_usage_log",
        indexes = {
                @Index(name = "idx_meme_usage_log_meme", columnList = "meme_id"),
                @Index(name = "idx_meme_usage_log_user", columnList = "user_id"),
                @Index(name = "idx_meme_usage_log_created_at", columnList = "created_at")
        }
)
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemeUsageLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "meme_usage_id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "meme_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_meme_usage_log_meme")
    )
    private Meme meme;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_meme_usage_log_user")
    )
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "usage_type", nullable = false, length = 20)
    private MemeUsageType usageType; // USE, DOWNLOAD

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    // ---- 편의 메서드 ----
    public static MemeUsageLog use(User user, Meme meme) {
        return MemeUsageLog.builder()
                .user(user)
                .meme(meme)
                .usageType(MemeUsageType.USE)
                .build();
    }

    public static MemeUsageLog download(User user, Meme meme) {
        return MemeUsageLog.builder()
                .user(user)
                .meme(meme)
                .usageType(MemeUsageType.DOWNLOAD)
                .build();
    }
}
