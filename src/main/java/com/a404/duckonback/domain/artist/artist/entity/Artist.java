package com.a404.duckonback.domain.artist.artist.entity;

import com.a404.duckonback.common.entity.BaseLastModifiedAuditEntity;
import com.a404.duckonback.domain.artist.common.ArtistReadable;
import com.a404.duckonback.domain.room.entity.Room;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.*;

@Entity
@Table(name = "artist")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Artist extends BaseLastModifiedAuditEntity implements ArtistReadable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long artistId;

    @Column(name = "name_en", nullable = false, length = 100)
    private String nameEn;

    @Column(name = "name_kr", nullable = false, length = 100)
    private String nameKr;

    @Column(name = "debut_date", nullable = false)
    private LocalDate debutDate;

    @Column(name = "img_url", columnDefinition = "TEXT")
    private String imgUrl;

    /**
     * OFFICIAL: 메인 노출
     * DEMOTED : 강등되어 메인에서 숨김(= emerging에서만 보이도록 UI 처리)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ArtistStatus status;

    @OneToMany(mappedBy = "artist")
    private List<ArtistFollow> followers = new ArrayList<>();

    @OneToMany(mappedBy = "artist")
    private List<Room> rooms = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (this.status == null) this.status = ArtistStatus.OFFICIAL;
    }

    // 편의 메서드
    public void demote() { this.status = ArtistStatus.DEMOTED; }
    public void promote() { this.status = ArtistStatus.OFFICIAL; }

    @Override
    public Long getId() {
        return this.artistId;
    }

    @Override
    public String getNameEn(){
        return this.nameEn;
    }

    @Override
    public String getNameKr(){
        return this.nameKr;
    }
}
