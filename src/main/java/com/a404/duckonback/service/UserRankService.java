package com.a404.duckonback.service;

import com.a404.duckonback.dto.UserRankDTO;
import com.a404.duckonback.dto.UserRankLeaderboardDTO;

import java.util.List;

public interface UserRankService {
    UserRankDTO getUserRank(Long userPk);

    List<UserRankLeaderboardDTO> getUserRankLeaderboard(int page, int size);
}
