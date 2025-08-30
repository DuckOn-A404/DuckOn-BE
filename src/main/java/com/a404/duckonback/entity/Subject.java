package com.a404.duckonback.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
    name = "subject",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_subject_slug", columnNames = {"slug"})
    },
    indexes = {
        @Index(name = "idx_subject_domain",       columnList = "domain_id"),
        @Index(name = "idx_subject_primary_cat",  columnList = "primary_category_id")
    }
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subject_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "domain_id", nullable = false)
    private Domain domain;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "primary_category_id")
    private Category primaryCategory;

    /** 원어 로케일 (예: ko, en, ja ...) */
    @Column(name = "native_locale", nullable = false, length = 20)
    private String nativeLocale;

    /** ISO-3166-1 alpha-2 국가코드 (예: KR, JP, US) */
    @Column(name = "country_code", nullable = false, length = 2)
    private String countryCode;

    /** URL용 불변 슬러그 (영어 기반, 생성 후 변경하지 않음) */
    @Column(name = "slug", nullable = false, length = 120)
    private String slug;

    @Column(name = "debut_date")
    private LocalDate debutDate;

    @Lob
    @Column(name = "img_url")
    private String imgUrl;

    @Builder.Default
    @ManyToMany
    @JoinTable(
        name = "subject_category_map",
        joinColumns = @JoinColumn(name = "subject_id"),
        inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<Category> categories = new HashSet<>();

    /** 다국어 이름들 (subject_name) */
    @Builder.Default
    @OneToMany(mappedBy = "subject", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<SubjectName> names = new HashSet<>();

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    /** 연관 편의 메서드 */
    public void addName(SubjectName name) {
        if (name == null) return;
        name.setSubject(this);
        this.names.add(name);
    }

    public void removeName(SubjectName name) {
        if (name == null) return;
        this.names.remove(name);
        name.setSubject(null);
    }
}
