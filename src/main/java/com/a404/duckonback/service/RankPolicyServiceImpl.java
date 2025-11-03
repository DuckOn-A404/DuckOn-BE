package com.a404.duckonback.service;

import com.a404.duckonback.entity.RankPolicy;
import com.a404.duckonback.repository.RankPolicyRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RankPolicyServiceImpl implements RankPolicyService{

    private final RankPolicyRepository rankPolicyRepository;
    private volatile List<RankPolicy> cachedRankPolicies = List.of(
            // 안전 기본값 (DB 비어있을 때 대비)
            make("TIER_1", 50), make("TIER_2", 30), make("NORMAL", 0)
    );

    private static RankPolicy make(String rankLevel, int min) {
        RankPolicy rp = new RankPolicy();
        rp.setLevel(rankLevel);
        rp.setMinRoomCreateCount(min);
        rp.setEnabled(true);
        rp.setPriority(0);
        rp.setUpdatedAt(LocalDateTime.now());
        return rp;
    }

    @Override
    @PostConstruct
    public void load() {
        refresh();
    }

    @Override
    public void refresh() {
        List<RankPolicy> list = rankPolicyRepository.findAllByEnabledTrueOrderByMinRoomCreateCountDesc();
        if(!list.isEmpty()) cachedRankPolicies = List.copyOf(list);
    }

    @Override
    public String resolveRankLevel(Long createCount) {
        for(RankPolicy rp : cachedRankPolicies) {
            if(createCount >= rp.getMinRoomCreateCount()) {
                return rp.getLevel();
            }
        }
        return "NORMAL";
    }

    @Override
    public List<RankPolicy> currentRankPolicies() {
        return cachedRankPolicies;
    }
}
