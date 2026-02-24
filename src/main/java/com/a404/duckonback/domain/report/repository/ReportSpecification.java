package com.a404.duckonback.domain.report.repository;

import org.springframework.data.jpa.domain.Specification;

import com.a404.duckonback.common.enums.ReportStatus;
import com.a404.duckonback.common.enums.ReportType;
import com.a404.duckonback.domain.report.entity.Report;

public class ReportSpecification {
    public static Specification<Report> hasReporterUserId(String userId) {
        return (root, query, cb) -> {
            if (userId == null || userId.isBlank()) return null;
            return cb.equal(root.get("reporter").get("userId"), userId);
        };
    }

    public static Specification<Report> hasReportedUserId(String userId) {
        return (root, query, cb) -> {
            if (userId == null || userId.isBlank()) return null;
            return cb.equal(root.get("reported").get("userId"), userId);
        };
    }

    public static Specification<Report> hasStatus(ReportStatus status) {
        return (root, query, cb) -> {
            if (status == null) return null;
            return cb.equal(root.get("reportStatus"), status);
        };
    }

    public static Specification<Report> hasType(ReportType type) {
        return (root, query, cb) -> {
            if (type == null) return null;
            return cb.equal(root.get("reportType"), type);
        };
    }
}