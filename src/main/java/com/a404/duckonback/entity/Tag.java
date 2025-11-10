package com.a404.duckonback.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "tag",
        uniqueConstraints = @UniqueConstraint(name = "uk_tag_name", columnNames = "tag_name"),
        indexes = @Index(name = "idx_tag_name", columnList = "tag_name")
)
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tag_id", nullable = false)
    private Long id;

    @Column(name = "tag_name", nullable = false, length = 100)
    private String tagName;

    @Builder.Default
    @OneToMany(mappedBy = "tag")
    private Set<MemeTag> memeTags = new HashSet<>();
}
