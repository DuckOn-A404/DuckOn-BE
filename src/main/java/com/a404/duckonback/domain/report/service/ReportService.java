package com.a404.duckonback.domain.report.service;

import com.a404.duckonback.common.enums.ReportStatus;
import com.a404.duckonback.common.enums.ReportType;
import com.a404.duckonback.domain.report.entity.Report;
import com.a404.duckonback.domain.report.dto.ReportDTO;
import com.a404.duckonback.common.dto.PageResponse;

import java.util.List;
import java.util.Optional;

public interface ReportService {
    Report createReport(Report report);
    Optional<Report> getReportById(Long reportId);
    PageResponse<ReportDTO> getAllReports(int page, int size);
    Report updateReport(Long reportId, Report updatedReport);
    void deleteReport(Long reportId);

    List<Report> getReportsByReporter(Long id);
    List<Report> getReportsByReported(Long id);
    List<Report> getReportsByStatus(ReportStatus status);
    List<Report> getReportsByType(ReportType type);
}
