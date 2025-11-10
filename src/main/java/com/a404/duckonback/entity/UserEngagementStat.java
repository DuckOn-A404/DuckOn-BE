package com.a404.duckonback.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "user_engagement_stats")
public class UserEngagementStat {
    @Id
    @Column(name = "user_id", nullable = false)
    private Long id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "room_create_count", nullable = false)
    private Integer roomCreateCount;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "artist_chat_count", nullable = false)
    private Integer artistChatCount;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "meme_create_count", nullable = false)
    private Integer memeCreateCount;

    @Column(name = "p_room", precision = 5, scale = 2)
    private BigDecimal pRoom;

    @Column(name = "p_chat", precision = 5, scale = 2)
    private BigDecimal pChat;

    @Column(name = "p_meme", precision = 5, scale = 2)
    private BigDecimal pMeme;

    @Column(name = "p_composite", precision = 5, scale = 2)
    private BigDecimal pComposite;

    @Size(max = 10)
    @Column(name = "grade_room", length = 10)
    private String gradeRoom;

    @Size(max = 10)
    @Column(name = "grade_chat", length = 10)
    private String gradeChat;

    @Size(max = 10)
    @Column(name = "grade_meme", length = 10)
    private String gradeMeme;

    @Size(max = 10)
    @Column(name = "grade_composite", length = 10)
    private String gradeComposite;

    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP(6)")
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

}