package com.a404.duckonback.domain.report.entity;

import com.a404.duckonback.domain.user.entity.User;
import com.a404.duckonback.common.enums.ReportStatus;
import com.a404.duckonback.common.enums.ReportType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "report")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reportId;

    @ManyToOne
    @JoinColumn(name = "reported_user_id", nullable = false)
    private User reported;

    @ManyToOne
    @JoinColumn(name = "reporter_user_id", nullable = false)
    private User reporter;

    @Column(name = "content_id")
    private Long contentId;  // room_id, meme_id, message_id

    @Column(name = "reported_content", length = 255)
    private String reportedContent;  // 방제, 밈 url, 채팅 내용

    @Column(name = "reported_at", nullable = false)
    private LocalDateTime reportedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_status", nullable = false)
    private ReportStatus reportStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", nullable = false)
    private ReportType reportType;

    @Column(name = "report_reason", length = 255)
    private String reportReason;
}
