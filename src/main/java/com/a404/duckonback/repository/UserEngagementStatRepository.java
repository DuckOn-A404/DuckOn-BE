package com.a404.duckonback.repository;

import com.a404.duckonback.entity.UserEngagementStat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface UserEngagementStatRepository extends JpaRepository<UserEngagementStat, Long> {

    // 선택: 명시형
    List<UserEngagementStat> findByIdIn(Collection<Long> ids);

    // 또는 기본 제공: findAllById(Iterable<Long> ids)
}