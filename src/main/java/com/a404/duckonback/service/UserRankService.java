package com.a404.duckonback.service;

import com.a404.duckonback.dto.UserRankDTO;

import java.util.List;

public interface UserRankService {
    UserRankDTO getUserRank(Long userId);
    List<UserRankDTO> getLeaderboard(int page, int size);
}
