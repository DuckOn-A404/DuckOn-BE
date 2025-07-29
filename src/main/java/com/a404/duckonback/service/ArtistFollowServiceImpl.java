package com.a404.duckonback.service;

import com.a404.duckonback.entity.ArtistFollow;
import com.a404.duckonback.entity.ArtistFollowId;
import com.a404.duckonback.repository.ArtistFollowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ArtistFollowServiceImpl implements ArtistFollowService {

    private final ArtistFollowRepository artistFollowRepository;

    @Override
    public ArtistFollow createArtistFollow(ArtistFollow artistFollow) {
        return artistFollowRepository.save(artistFollow);
    }



    @Override
    public List<ArtistFollow> getFollowsByUser(Long id) {
        return artistFollowRepository.findByUser_Id(id);
    }

    @Override
    public List<ArtistFollow> getFollowsByArtist(Long artistId) {
        return artistFollowRepository.findByArtist_ArtistId(artistId);
    }

    @Override
    public void deleteArtistFollow(Long id, Long artistId) {
        artistFollowRepository.deleteByUser_IdAndArtist_ArtistId(id, artistId);
    }

    @Override
    public boolean isFollowingArtist(Long id, Long artistId) {
        return artistFollowRepository.existsByUser_IdAndArtist_ArtistId(id, artistId);
    }
}
