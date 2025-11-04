package com.a404.duckonback.entity;

import com.a404.duckonback.enums.SocialProvider;
import com.a404.duckonback.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "user",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_user_id", columnNames = {"user_id"}),
                @UniqueConstraint(name = "uk_user_email", columnNames = {"email"}),
                @UniqueConstraint(name = "uk_user_provider_pid", columnNames = {"provider", "provider_id"})
        }
)
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "user_id", nullable = false, unique = true, length = 50)
    private String userId;

    @Column(name = "password", nullable = false, length = 255)
    private String password; // OAuth 계정은 더미/랜덤 해시 보관

    @Column(name = "nickname", nullable = false, length = 100)
    private String nickname;

    @Column(name = "provider", length = 20)
    @Enumerated(EnumType.STRING)
    private SocialProvider provider; // GOOGLE, KAKAO, NAVER, LOCAL

    @Column(name = "provider_id", length = 255)
    private String providerId;

    @Column(name = "has_local_credential")
    private Boolean hasLocalCredential; // ID/PW 직접 설정 여부

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole role;

    @Column(name = "language", nullable = false, length = 2)
    private String language;

    @Column(name = "img_url", columnDefinition = "TEXT")
    private String imgUrl;

    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

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

    @Builder.Default
    @OneToMany(mappedBy = "creator")
    private List<Meme> memes = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Boolean getDeleted() { return deleted; } // 래퍼형으로도 리턴
    public boolean isDeleted() { return deleted; }

}
