package com.a404.duckonback.domain.tag.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "tag_search_log",
        indexes = {
            @Index(name = "idx_tag_search_log_tag", columnList = "tag_id"),
            @Index(name = "idx_tag_search_log_searched_at", columnList = "searched_at")
        }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TagSearchLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tag_id")
    private Tag tag;

    @Column(name = "searched_at", nullable = false, updatable = false)
    private LocalDateTime searchedAt;

    @Column(name = "keyword_raw", length = 255)
    private String keywordRaw;

    @PrePersist
    protected void onCreate() {
        if (this.searchedAt == null) {
            this.searchedAt = LocalDateTime.now();
        }
    }
}
