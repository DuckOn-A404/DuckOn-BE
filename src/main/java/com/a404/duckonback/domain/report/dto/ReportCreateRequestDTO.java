package com.a404.duckonback.domain.report.dto;

import java.time.LocalDateTime;

import com.a404.duckonback.common.enums.ReportStatus;
import com.a404.duckonback.common.enums.ReportType;
import com.a404.duckonback.domain.report.entity.Report;
import com.a404.duckonback.domain.user.entity.User;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportCreateRequestDTO {
    private String reportedId;
    private Long contentId;
    private String reportedContent;
    private ReportType reportType;
    private String reportReason;

    public Report toEntity(User reporter, User reported) {
        return Report.builder()
                .reporter(reporter)
                .reported(reported)
                .contentId(contentId)
                .reportedContent(reportedContent)
                .reportType(reportType)
                .reportReason(reportReason)
                .reportedAt(LocalDateTime.now())
                .reportStatus(ReportStatus.PENDING)
                .build();
    }
}
