package com.a404.duckonback.domain.artist.artist.entity;

import com.a404.duckonback.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@IdClass(ArtistFollowId.class)
@Table(name = "artist_follow")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ArtistFollow {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artist_id", nullable = false)
    private Artist artist;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}

