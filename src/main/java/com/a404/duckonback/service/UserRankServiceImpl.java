package com.a404.duckonback.service;

import com.a404.duckonback.dto.UserRankDTO;
import com.a404.duckonback.dto.UserRankLeaderboardDTO;
import com.a404.duckonback.entity.User;
import com.a404.duckonback.enums.RankLevel;
import com.a404.duckonback.exception.CustomException;
import com.a404.duckonback.repository.UserEngagementStatsJdbcRepository;
import com.a404.duckonback.repository.UserEngagementStatsJdbcRepository.LeaderboardRow;
import com.a404.duckonback.repository.UserEngagementStatsJdbcRepository.SnapshotRow;
import com.a404.duckonback.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserRankServiceImpl implements UserRankService{

    private final UserRepository userRepository;
    private final UserEngagementStatsJdbcRepository userEngagementStatsJdbcRepository;

    /** 단일 유저 등급 조회: userEngagementStats 스냅샷 사용 */
    @Override
    @Transactional(readOnly = true)
    public UserRankDTO getUserRank(Long userPk) {
        User user = userRepository.findByIdAndDeletedFalse(userPk);
        if(user == null) {
            throw new CustomException("User not found", HttpStatus.NOT_FOUND);
        }

        SnapshotRow snapshotRow = userEngagementStatsJdbcRepository.findByUserPk(userPk)
                .orElseGet(() -> {
                    SnapshotRow newRow = new SnapshotRow();
                    newRow.userIdPk = userPk;
                    newRow.roomCreateCount = 0L;
                    newRow.gradeComposite = RankLevel.GREEN.name();
                    return newRow;
                });

        return UserRankDTO.builder()
                .roomCreateCount(snapshotRow.roomCreateCount)
                .rankLevel(RankLevel.fromString(snapshotRow.gradeComposite))
                .build();
    }

    /** 리더보드 조회: 스냅샷 기반 */
    @Override
    @Transactional(readOnly = true)
    public List<UserRankLeaderboardDTO> getUserRankLeaderboard(int page, int size) {
        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(1, size), 100); // 최대 100개 제한
        int offset = safePage * safeSize;

        List<LeaderboardRow> leaderboardRows = userEngagementStatsJdbcRepository.findLeaderboard(safeSize, offset);

        return leaderboardRows.stream().map(row -> UserRankLeaderboardDTO.builder()
                        .userId(row.publicUserId)
                        .nickname(row.nickname)
                        .profileImgUrl(row.imgUrl)
                        .userRank(UserRankDTO.builder()
                                .roomCreateCount(row.roomCreateCount)
                                .rankLevel(RankLevel.fromString(row.gradeComposite))
                        .build())
                .build()
                ).toList();
    }
}
