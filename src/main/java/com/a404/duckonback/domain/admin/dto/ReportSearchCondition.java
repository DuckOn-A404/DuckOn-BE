package com.a404.duckonback.domain.admin.dto;

import com.a404.duckonback.common.enums.ReportStatus;
import com.a404.duckonback.common.enums.ReportType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReportSearchCondition {
    private String reporterUserId;
    private String reportedUserId;
    private ReportStatus status;
    private ReportType type;

}
