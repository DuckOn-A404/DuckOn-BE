package com.a404.duckonback.domain.report.service;

import com.a404.duckonback.common.enums.ReportStatus;
import com.a404.duckonback.common.enums.ReportType;
import com.a404.duckonback.domain.report.entity.Report;

import java.util.List;
import java.util.Optional;

public interface ReportService {
    Report createReport(Report report);
    Optional<Report> getReportById(Long reportId);
    List<Report> getAllReports();
    Report updateReport(Long reportId, Report updatedReport);
    void deleteReport(Long reportId);

    List<Report> getReportsByReporter(Long id);
    List<Report> getReportsByReported(Long id);
    List<Report> getReportsByStatus(ReportStatus status);
    List<Report> getReportsByType(ReportType type);
}
