package com.a404.duckonback.entity;

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
public class Artist {

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

    @OneToMany(mappedBy = "artist")
    private List<ArtistFollow> followers = new ArrayList<>();

    @OneToMany(mappedBy = "artist")
    private List<Room> rooms = new ArrayList<>();
}
