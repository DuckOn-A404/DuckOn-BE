package com.a404.duckonback.domain.report.repository;

import com.a404.duckonback.common.enums.ReportStatus;
import com.a404.duckonback.common.enums.ReportType;
import com.a404.duckonback.domain.report.entity.Report;
import com.a404.duckonback.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    Page<Report> findByReporter_UserId(String userId, Pageable pageable);
    Page<Report> findByReported_UserId(String userId, Pageable pageable);
    Page<Report> findByReportStatus(ReportStatus status, Pageable pageable);
    Page<Report> findByReportType(ReportType contentType, Pageable pageable);
    boolean existsByReporterAndContentIdAndReportType(User reporter, Long contentId, ReportType
            reportType);
}
