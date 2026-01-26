package com.a404.duckonback.domain.user.service;

import com.a404.duckonback.domain.user.dto.UserRankDTO;
import com.a404.duckonback.domain.user.dto.UserRankLeaderboardDTO;

import java.util.List;

public interface UserRankService {
    UserRankDTO getUserRank(Long userPk);

    List<UserRankLeaderboardDTO> getUserRankLeaderboard(int page, int size);
}
