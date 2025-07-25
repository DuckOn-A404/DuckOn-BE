package com.a404.duckonback.service;

import com.a404.duckonback.entity.ArtistFollow;

import java.util.List;
import java.util.Optional;

public interface ArtistFollowService {
    ArtistFollow createArtistFollow(ArtistFollow artistFollow);
    Optional<ArtistFollow> getArtistFollow(Long id, Integer artistId);
    List<ArtistFollow> getFollowsByUser(Long id);
    List<ArtistFollow> getFollowsByArtist(Integer artistId);
    void deleteArtistFollow(Long id, Integer artistId);
    boolean isFollowingArtist(Long id, Integer artistId);
}
