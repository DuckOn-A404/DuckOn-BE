package com.a404.duckonback.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "meme_favorite",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_mf_user_meme", columnNames = {"user_id", "meme_id"})
        },
        indexes = {
                @Index(name = "idx_mf_user", columnList = "user_id, created_at DESC"),
                @Index(name = "idx_mf_meme", columnList = "meme_id")
        }
)
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemeFavorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_mf_user")
    )
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "meme_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_mf_meme")
    )
    private Meme meme;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
