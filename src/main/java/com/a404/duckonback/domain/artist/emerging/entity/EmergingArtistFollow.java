package com.a404.duckonback.domain.artist.emerging.entity;

import com.a404.duckonback.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@IdClass(EmergingArtistFollowId.class)
@Table(name = "emerging_artist_follow")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmergingArtistFollow {
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emerging_artist_id", nullable = false)
    private EmergingArtist emergingArtist;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
    }
}
