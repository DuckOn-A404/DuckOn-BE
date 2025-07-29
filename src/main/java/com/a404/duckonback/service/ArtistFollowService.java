package com.a404.duckonback.service;

import com.a404.duckonback.entity.ArtistFollow;

import java.util.List;
import java.util.Optional;

public interface ArtistFollowService {
    ArtistFollow createArtistFollow(ArtistFollow artistFollow);
    List<ArtistFollow> getFollowsByUser(Long id);
    List<ArtistFollow> getFollowsByArtist(Long artistId);
    void deleteArtistFollow(Long id, Long artistId);
    boolean isFollowingArtist(Long id, Long artistId);
}
