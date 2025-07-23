package com.a404.duckonback.entity;

import com.a404.duckonback.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @Column(name = "uuid", nullable = false, length = 255)
    private String uuid;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "user_id", nullable = false, unique = true, length = 50)
    private String userId;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "nickname", nullable = false, length = 100)
    private String nickname;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole role;

    @Column(name = "language", nullable = false, length = 2)
    private String language;

    @Column(name = "img_url", columnDefinition = "TEXT")
    private String imgUrl;

    @Builder.Default
    @OneToMany(mappedBy = "reporter")
    private List<Report> reportsAsReporter = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "reported")
    private List<Report> reportsAsReported = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "blocker")
    private List<UserBlock> blockedUsers = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "blocked")
    private List<UserBlock> blockedByUsers = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "follower")
    private List<Follow> following = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "following")
    private List<Follow> followers = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "user")
    private List<ArtistFollow> artistFollows = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "user")
    private List<Penalty> penalties = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "creator")
    private List<Room> rooms = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
