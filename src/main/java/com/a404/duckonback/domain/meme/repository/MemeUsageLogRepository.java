package com.a404.duckonback.domain.meme.repository;

import com.a404.duckonback.domain.meme.entity.MemeUsageLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemeUsageLogRepository extends JpaRepository<MemeUsageLog, Long> {
}
