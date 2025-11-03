package com.a404.duckonback.repository;

import com.a404.duckonback.entity.RankPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RankPolicyRepository extends JpaRepository<RankPolicy, Long> {
    List<RankPolicy> findAllByEnabledTrueOrderByMinRoomCreateCountDesc();
}
