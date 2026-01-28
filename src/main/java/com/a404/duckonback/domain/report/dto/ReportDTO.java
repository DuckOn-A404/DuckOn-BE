package com.a404.duckonback.domain.report.dto;

import com.a404.duckonback.domain.report.entity.Report;

import java.time.LocalDateTime;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportDTO {
    private Long reportId;
    private String reporterId;
    private String reportedId;
    private String reportedContent;
    private LocalDateTime reportedAt;
    private String reportStatus;
    private String reportType;
    private String reportReason;

    public static ReportDTO fromEntity(Report report) {
        return ReportDTO.builder()
                .reportId(report.getReportId())
                .reporterId(report.getReporter().getUserId())
                .reportedId(report.getReported().getUserId())
                .reportedContent(report.getReportedContent())
                .reportedAt(report.getReportedAt())
                .reportStatus(report.getReportStatus().name())
                .reportType(report.getReportType().name())
                .reportReason(report.getReportReason())
                .build();
    }
}
