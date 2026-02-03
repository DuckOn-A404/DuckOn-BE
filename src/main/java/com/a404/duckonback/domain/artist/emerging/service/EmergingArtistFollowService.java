package com.a404.duckonback.domain.artist.emerging.service;

public interface EmergingArtistFollowService {
    void followEmergingArtist(Long userId, Long emergingArtistId);
    void unfollowEmergingArtist(Long userId, Long emergingArtistId);
}
