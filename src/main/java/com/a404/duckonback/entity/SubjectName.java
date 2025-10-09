package com.a404.duckonback.entity;

import com.a404.duckonback.enums.SubjectNameType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "subject_name",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_sn_subject_locale_name",
            columnNames = {"subject_id", "locale_tag", "name"}
        )
    },
    indexes = {
        @Index(name = "idx_sn_subject", columnList = "subject_id"),
        @Index(name = "idx_sn_locale",  columnList = "locale_tag"),
        @Index(name = "idx_sn_name",    columnList = "name")
    }
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class SubjectName {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "name_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    /** BCP47 태그: ko, en, ja, ja-Kana 등 */
    @Column(name = "locale_tag", nullable = false, length = 20)
    private String localeTag;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "name_type", nullable = false, length = 20)
    @Builder.Default
    private SubjectNameType nameType = SubjectNameType.OFFICIAL;

    /** 해당 locale 안에서 대표 여부 */
    @Column(name = "is_primary", nullable = false)
    @Builder.Default
    private boolean primary = true;

    /** 같은 locale/name_type 내 우선순위 (낮을수록 우선) */
    @Column(name = "priority", nullable = false)
    @Builder.Default
    private short priority = 0;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
