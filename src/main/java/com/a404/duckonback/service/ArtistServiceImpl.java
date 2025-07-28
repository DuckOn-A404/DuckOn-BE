package com.a404.duckonback.service;

import com.a404.duckonback.dto.ArtistDTO;
import com.a404.duckonback.entity.Artist;
import com.a404.duckonback.entity.ArtistFollow;
import com.a404.duckonback.entity.User;
import com.a404.duckonback.exception.CustomException;
import com.a404.duckonback.repository.ArtistFollowRepository;
import com.a404.duckonback.repository.ArtistRepository;
import com.a404.duckonback.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Override
    public List<Integer> findAllArtistIdByUserId(Long id){
        return artistRepository.findAllArtistIdByUserId(id);
    }

    @Override
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

    @Override
    public Page<ArtistDTO> getArtists(Pageable pageable) {
        return artistRepository.findAll(pageable)
                .map(artist -> {
                    long cnt = artistFollowRepository.countByArtist_ArtistId(artist.getArtistId());
                    return ArtistDTO.fromEntity(artist, cnt);
                });
    }

    @Override
    public List<ArtistDTO> searchArtists(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new CustomException("keyword는 필수 파라미터입니다.", HttpStatus.BAD_REQUEST);
        }
        return artistRepository.searchByKeyword(keyword.trim()).stream()
                .map(artist -> {
                    long cnt = artistFollowRepository.countByArtist_ArtistId(artist.getArtistId());
                    return ArtistDTO.fromEntity(artist, cnt);
                })
                .toList();
    }

    @Override
    public List<ArtistDTO> getRandomArtists(int size) {
        if (size < 1) {
            throw new CustomException("size는 1 이상의 정수여야 합니다.", HttpStatus.BAD_REQUEST);
        }
        return artistRepository.findRandomArtists(size).stream()
                .map(artist -> {
                    long cnt = artistFollowRepository.countByArtist_ArtistId(artist.getArtistId());
                    return ArtistDTO.fromEntity(artist, cnt);
                })
                .toList();
    }

}
