package com.a404.duckonback.domain.report.repository;

import com.a404.duckonback.common.enums.ReportStatus;
import com.a404.duckonback.common.enums.ReportType;
import com.a404.duckonback.domain.report.entity.Report;
import com.a404.duckonback.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByReported_Id(Long id);
    Page<Report> findByReporter_Id(Long id, Pageable pageable);
    List<Report> findByReportStatus(ReportStatus status);
    List<Report> findByReportType(ReportType type);
    boolean existsByReporterAndContentIdAndReportType(User reporter, Long contentId, ReportType
            reportType);
}
