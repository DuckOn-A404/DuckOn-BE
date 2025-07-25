package com.a404.duckonback.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@IdClass(ArtistFollowId.class)
@Table(name = "artist_follow")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArtistFollow {

    @Id
    @ManyToOne
    @JoinColumn(name = "id", nullable = false)
    private User user;

    @Id
    @ManyToOne
    @JoinColumn(name = "artist_id", nullable = false)
    private Artist artist;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
