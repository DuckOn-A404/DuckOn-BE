package com.a404.duckonback.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="subject")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subject {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name="subject_id")
    private Long id;

    @ManyToOne(fetch= FetchType.LAZY) @JoinColumn(name="domain_id", nullable=false)
    private Domain domain;

    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="primary_category_id")
    private Category primaryCategory;

    private LocalDate debutDate;
    @Lob
    private String imgUrl;

    @Column(nullable=false, length=100) private String nameEn;
    @Column(nullable=false, length=100) private String nameKr;

    @Builder.Default
    @ManyToMany
    @JoinTable(name="subject_category_map",
        joinColumns=@JoinColumn(name="subject_id"),
        inverseJoinColumns=@JoinColumn(name="category_id"))
    private Set<Category> categories = new HashSet<>();

    @Column(name="created_at") private LocalDateTime createdAt;
    @PrePersist
    void onCreate(){ if(createdAt==null) createdAt=LocalDateTime.now(); }
}
