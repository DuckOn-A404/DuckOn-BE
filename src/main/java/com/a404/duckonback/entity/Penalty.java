package com.a404.duckonback.entity;

import com.a404.duckonback.enums.PenaltyStatus;
import com.a404.duckonback.enums.PenaltyType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "penalty")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Penalty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "penalty_id", nullable = false)
    private Long penaltyId;

    @Column(name = "start_at")
    private LocalDateTime startAt;

    @Column(name = "end_at")
    private LocalDateTime endAt;

    @Column(name = "reason", nullable = false, length = 255)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "penalty_type", nullable = false)
    private PenaltyType penaltyType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PenaltyStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
