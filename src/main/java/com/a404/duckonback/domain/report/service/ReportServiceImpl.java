package com.a404.duckonback.domain.report.service;

import com.a404.duckonback.common.dto.PageResponse;
import com.a404.duckonback.common.enums.ReportStatus;
import com.a404.duckonback.common.enums.ReportType;
import com.a404.duckonback.common.exception.CustomException;
import com.a404.duckonback.common.response.ErrorCode;
import com.a404.duckonback.domain.report.dto.ReportDTO;
import com.a404.duckonback.domain.report.dto.ReportCreateRequestDTO;
import com.a404.duckonback.domain.report.entity.Report;
import com.a404.duckonback.domain.report.repository.ReportRepository;
import com.a404.duckonback.domain.user.entity.User;
import com.a404.duckonback.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;

    @Override
    public Report createReport(ReportCreateRequestDTO request, User reporter) {
        User reported = userRepository.findByUserIdAndDeletedFalse(request.getReportedId()); 
        
        // 같은 사용자가 동일 컨텐츠 중복 신고 안되게
        if (reportRepository.existsByReporterAndContentIdAndReportType(reporter,
                request.getContentId(), request.getReportType())) {
            throw new CustomException(ErrorCode.DUPLICATE_REPORT);
        }
        // todo: 자기 자신 신고 방지
        if (reporter.getId().equals(reported.getId())) {
            throw new CustomException(ErrorCode.SELF_REPORT);
        }
        // todo: contentId 유효성 검사

        return reportRepository.save(request.toEntity(reporter, reported));
    }

    @Override
    public Optional<Report> getReportById(Long reportId) {
        return reportRepository.findById(reportId);
    }

    @Override
    public PageResponse<ReportDTO> getAllReports(int page, int size) {
        int safePage = Math.max(page - 1, 0); // 페이지 번호는 0부터 시작
        int safeSize = Math.min(Math.max(size, 1), 100); // 페이지 크기는 1에서 100 사이로 제한

        Pageable pageable = PageRequest.of(safePage, safeSize);
        Page<Report> pageResult = reportRepository.findAll(pageable);

        int totalPages = pageResult.getTotalPages();
        if (safePage >= totalPages && totalPages > 0) {
            throw new CustomException(ErrorCode.EXCEED_TOTAL_PAGES);
        }

        Page<ReportDTO> dtoPage = pageResult.map(ReportDTO::fromEntity);
        return PageResponse.from1Base(dtoPage);
    }

    @Override
    public ReportDTO getReportDetail(Long reportId) {
        return reportRepository.findById(reportId)
                .map(ReportDTO::fromEntity)
                .orElseThrow(() -> new CustomException(ErrorCode.REPORT_NOT_FOUND));
    }

    @Override
    public PageResponse<ReportDTO> getReportsByReporter(String userId, int page, int size) {
        int safePage = Math.max(page - 1, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by("reportedAt").descending());
        Page<Report> pageResult = reportRepository.findByReporter_UserId(userId, pageable);
        return PageResponse.from1Base(pageResult.map(ReportDTO::fromEntity));
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
    public PageResponse<ReportDTO> getReportsByReported(String userId, int page, int size) {
        int safePage = Math.max(page - 1, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by("reportedAt").descending());
        Page<Report> pageResult = reportRepository.findByReported_UserId(userId, pageable);
        return PageResponse.from1Base(pageResult.map(ReportDTO::fromEntity));
    }

    @Override
    public PageResponse<ReportDTO> getReportsByStatus(ReportStatus status, int page, int size) {
        int safePage = Math.max(page - 1, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by("reportedAt").descending());
        Page<Report> pageResult = reportRepository.findByReportStatus(status, pageable);
        return PageResponse.from1Base(pageResult.map(ReportDTO::fromEntity));
    }

    @Override
    public PageResponse<ReportDTO> getReportsByContentType(ReportType contentType, int page, int size) {
        int safePage = Math.max(page - 1, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by("reportedAt").descending());
        Page<Report> pageResult = reportRepository.findByReportType(contentType, pageable);
        return PageResponse.from1Base(pageResult.map(ReportDTO::fromEntity));
    }

}
