package com.a404.duckonback.service;

import com.a404.duckonback.dto.UserRankDTO;
import com.a404.duckonback.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserRankServiceImpl implements UserRankService{
    private final RoomRepository roomRepository;
    private final RankPolicyService rankPolicyService;

    /** 단일 유저 등급 조회 **/
    @Override
    @Transactional(readOnly = true)
    public UserRankDTO getUserRank(Long userId) {
        long createCount = roomRepository.countByCreator_Id(userId);
        String rankLevel = rankPolicyService.resolveRankLevel(createCount);
        return UserRankDTO.builder()
                .id(userId)
                .rankLevel(rankLevel)
                .roomCreateCount(createCount)
                .build();
    }

    /** 리더보드 조회 **/
    @Override
    public List<UserRankDTO> getLeaderboard(int page, int size) {
        int offset = Math.max(0, page) * Math.max(1, size);
        List<Object[]> rows = roomRepository.findLeaderboard(size, offset);
        return rows.stream().map(row -> {
            Long userId = ((Number) row[0]).longValue();
            Long createCount = ((Number) row[1]).longValue();
            String rankLevel = rankPolicyService.resolveRankLevel(createCount);
            return UserRankDTO.builder()
                    .id(userId)
                    .rankLevel(rankLevel)
                    .roomCreateCount(createCount)
                    .build();
        }).toList();
    }
}
