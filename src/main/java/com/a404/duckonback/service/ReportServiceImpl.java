package com.a404.duckonback.service;

import com.a404.duckonback.entity.Report;
import com.a404.duckonback.enums.ReportStatus;
import com.a404.duckonback.enums.ReportType;
import com.a404.duckonback.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;

    @Override
    public Report createReport(Report report) {
        return reportRepository.save(report);
    }

    @Override
    public Optional<Report> getReportById(Long reportId) {
        return reportRepository.findById(reportId);
    }

    @Override
    public List<Report> getAllReports() {
        return reportRepository.findAll();
    }

    @Override
    public Report updateReport(Long reportId, Report updatedReport) {
        return reportRepository.findById(reportId)
                .map(report -> {
                    report.setReported(updatedReport.getReported());
                    report.setReporter(updatedReport.getReporter());
                    report.setReportedAt(updatedReport.getReportedAt());
                    report.setReportedContent(updatedReport.getReportedContent());
                    report.setReportReason(updatedReport.getReportReason());
                    report.setReportStatus(updatedReport.getReportStatus());
                    report.setReportType(updatedReport.getReportType());
                    return reportRepository.save(report);
                })
                .orElseThrow(() -> new IllegalArgumentException("Report not found with ID: " + reportId));
    }

    @Override
    public void deleteReport(Long reportId) {
        reportRepository.deleteById(reportId);
    }

    @Override
    public List<Report> getReportsByReporter(Long id) {
        return reportRepository.findByReporter_Id(id);
    }

    @Override
    public List<Report> getReportsByReported(Long id) {
        return reportRepository.findByReported_Id(id);
    }

    @Override
    public List<Report> getReportsByStatus(ReportStatus status) {
        return reportRepository.findByReportStatus(status);
    }

    @Override
    public List<Report> getReportsByType(ReportType type) {
        return reportRepository.findByReportType(type);
    }
}
