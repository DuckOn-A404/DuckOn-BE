package com.a404.duckonback.repository;

import com.a404.duckonback.entity.MemeUsageLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemeUsageLogRepository extends JpaRepository<MemeUsageLog, Long> {
}
