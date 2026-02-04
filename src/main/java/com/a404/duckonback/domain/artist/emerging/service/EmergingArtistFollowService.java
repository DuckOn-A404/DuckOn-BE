package com.a404.duckonback.domain.artist.emerging.service;

import com.a404.duckonback.common.dto.PageResponse;
import com.a404.duckonback.domain.artist.emerging.dto.EmergingArtistListResponseDTO;

public interface EmergingArtistFollowService {
    void followEmergingArtist(Long userId, Long emergingArtistId);
    void unfollowEmergingArtist(Long userId, Long emergingArtistId);
    PageResponse<EmergingArtistListResponseDTO> getFollowedEmergingArtists(Long userId, int page, int size);
}
