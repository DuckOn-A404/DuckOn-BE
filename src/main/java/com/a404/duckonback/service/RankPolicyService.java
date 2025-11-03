package com.a404.duckonback.service;

import com.a404.duckonback.entity.RankPolicy;

import java.util.List;

public interface RankPolicyService {
    void load();
    void refresh();
    String resolveRankLevel(Long createCount);
    List<RankPolicy> currentRankPolicies();
}
