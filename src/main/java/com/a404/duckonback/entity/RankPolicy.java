package com.a404.duckonback.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "rank_policy")
@Getter
@Setter
public class RankPolicy {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String level; // e.g., TIER_1, TIER_2, NORMAL
    private int minRoomCreateCount;
    private boolean enabled;
    private int priority;
    private String label;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

}
