package com.a404.duckonback.domain.report.repository;

import com.a404.duckonback.common.enums.ReportStatus;
import com.a404.duckonback.common.enums.ReportType;
import com.a404.duckonback.domain.report.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByReported_Id(Long id);
    List<Report> findByReporter_Id(Long id);
    List<Report> findByReportStatus(ReportStatus status);
    List<Report> findByReportType(ReportType type);
}
