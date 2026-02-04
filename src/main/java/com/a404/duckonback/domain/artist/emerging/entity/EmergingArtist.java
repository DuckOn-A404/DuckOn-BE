package com.a404.duckonback.domain.artist.emerging.entity;

import com.a404.duckonback.domain.artist.artist.entity.Artist;
import com.a404.duckonback.domain.artist.common.ArtistReadable;
import com.a404.duckonback.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "emerging_artist",
        indexes = {
                @Index(name = "idx_emerging_artist_status", columnList = "status"),
                @Index(name = "idx_emerging_artist_created_by", columnList = "created_by_user_id"),
                @Index(name = "idx_emerging_artist_linked_artist", columnList = "linked_artist_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmergingArtist implements ArtistReadable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "emerging_artist_id")
    private Long emergingArtistId;

    @Column(name = "name_en", nullable = false, length = 100)
    private String nameEn;

    @Column(name = "name_kr", nullable = false, length = 100)
    private String nameKr;

    /**
     * 라이징은 데뷔일이 확실치 않을 수 있어서 nullable
     */
    @Column(name = "debut_date")
    private LocalDate debutDate;

    @Column(name = "img_url", columnDefinition = "TEXT")
    private String imgUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "emergingArtist")
    @Builder.Default
    private List<EmergingArtistFollow> followers = new ArrayList<>();


    /**
     * 승격/강등 추적용 연결
     * - 승격 시 linkedArtist=해당 artist, status=PROMOTED
     * - 강등 시 linkedArtist=해당 artist(유지), status=ACTIVE
     * - 유저가 만든 순수 emerging은 linkedArtist=null, status=ACTIVE
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "linked_artist_id")
    private Artist linkedArtist;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private EmergingArtistStatus status;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
        if (this.status == null) this.status = EmergingArtistStatus.ACTIVE;
    }

    // --- 편의 메서드 ---
    public void softDelete() {
        this.status = EmergingArtistStatus.DELETED;
    }

    public void markPromoted(Artist artist) {
        this.linkedArtist = artist;
        this.status = EmergingArtistStatus.PROMOTED;
    }

    public void markActive() {
        this.status = EmergingArtistStatus.ACTIVE;
    }

    @Override
    public Long getId() {
        return this.emergingArtistId;
    }

    @Override
    public String getNameEn() {
        return this.nameEn;
    }

    @Override
    public String getNameKr() {
        return this.nameKr;
    }
}
