package com.a404.duckonback.repository;

import com.a404.duckonback.entity.Report;
import com.a404.duckonback.enums.ReportStatus;
import com.a404.duckonback.enums.ReportType;
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
