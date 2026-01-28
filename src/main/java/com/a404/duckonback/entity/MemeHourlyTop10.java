package com.a404.duckonback.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "meme_hourly_top10")
public class MemeHourlyTop10 {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @Column(name = "hour_start", nullable = false)
    private Instant hourStart;

    @NotNull
    @Column(name = "rank_no", nullable = false)
    private Integer rankNo;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "meme_id", nullable = false)
    private Meme meme;

    @NotNull
    @Column(name = "use_count", nullable = false)
    private Integer useCount;

    @NotNull
    @Column(name = "download_count", nullable = false)
    private Integer downloadCount;

    @NotNull
    @Column(name = "total_count", nullable = false)
    private Integer totalCount;

    @NotNull
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

}