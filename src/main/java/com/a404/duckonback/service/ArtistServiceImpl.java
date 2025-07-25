package com.a404.duckonback.service;

import com.a404.duckonback.entity.Artist;
import com.a404.duckonback.entity.ArtistFollow;
import com.a404.duckonback.entity.User;
import com.a404.duckonback.exception.CustomException;
import com.a404.duckonback.repository.ArtistFollowRepository;
import com.a404.duckonback.repository.ArtistRepository;
import com.a404.duckonback.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ArtistServiceImpl implements ArtistService {

    private final ArtistRepository artistRepository;
    private final UserRepository userRepository;
    private final ArtistFollowRepository artistFollowRepository;

    public List<Integer> findAllArtistIdByUserId(Long id){
        return artistRepository.findAllArtistIdByUserId(id);
    }

    public void followArtists(Long id, List<Integer> artistList){
        User user = userRepository.findById(id);

        for (Integer artistId : artistList) {
            Artist artist = artistRepository.findById(artistId)
                    .orElseThrow(() -> new CustomException("존재하지 않는 아티스트입니다. ID: " + artistId, HttpStatus.NOT_FOUND));


            ArtistFollow artistFollow = ArtistFollow.builder()
                    .user(user)
                    .artist(artist)
                    .createdAt(LocalDateTime.now())
                    .build();

            artistFollowRepository.save(artistFollow);
        }
    }
}
