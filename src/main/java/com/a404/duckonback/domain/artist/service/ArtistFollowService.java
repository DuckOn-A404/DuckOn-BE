package com.a404.duckonback.domain.artist.service;

import com.a404.duckonback.domain.artist.dto.FollowedArtistDTO;
import com.a404.duckonback.domain.artist.entity.ArtistFollow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ArtistFollowService {
    ArtistFollow createArtistFollow(ArtistFollow artistFollow);
    Optional<ArtistFollow> getArtistFollow(Long id, Long artistId);
    List<ArtistFollow> getFollowsByUser(Long id);
    List<ArtistFollow> getFollowsByArtist(Long artistId);
    void deleteArtistFollow(Long id, Long artistId);
    boolean isFollowingArtist(Long id, Long artistId);

    void followArtists(Long id, List<Long> artistList);

    Page<FollowedArtistDTO> getFollowedArtists(Long userId, Pageable pageable);

    void followArtist(Long userId, Long artistId);
    void unfollowArtist(Long userId, Long artistId);
    void updateArtistFollows(Long userId, List<Long> artistIds);


}
