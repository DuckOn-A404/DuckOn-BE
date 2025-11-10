package com.a404.duckonback.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "meme",
        indexes = {
                @Index(name = "idx_meme_creator_id", columnList = "creator_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Meme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "meme_id", nullable = false)
    private Long id;

    @Column(name = "usage_cnt", nullable = false)
    @Builder.Default
    private int usageCnt = 0;

    @Column(name = "download_cnt", nullable = false)
    @Builder.Default
    private int downloadCnt = 0;

    @Column(name = "image_url", nullable = false, length = 255)
    private String imageUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "creator_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_meme_creator_user")
    )
    private User creator;

    @OneToMany(mappedBy = "meme", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<MemeTag> memeTags = new HashSet<>();
}
