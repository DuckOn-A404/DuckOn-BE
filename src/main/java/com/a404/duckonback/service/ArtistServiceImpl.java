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
    public List<Long> findAllArtistIdByUserId(Long id){
        return artistRepository.findAllArtistIdByUserId(id);
    }

    @Override
    public void followArtists(Long id, List<Long> artistList){
        User user = userRepository.findById(id);

        for (Long artistId : artistList) {
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

    @Override
    public void followArtist(Long userId, Long artistId) {
        // 1) 사용자 존재 확인 (커스텀 findById → User or null)
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new CustomException("존재하지 않는 사용자입니다.", HttpStatus.NOT_FOUND);
        }

        // 2) 이미 팔로우했는지 검사
        if (artistFollowRepository.existsByUser_IdAndArtist_ArtistId(userId, artistId)) {
            throw new CustomException("이미 팔로우한 아티스트입니다.", HttpStatus.BAD_REQUEST);
        }

        // 3) 아티스트 존재 확인
        Artist artist = artistRepository.findById(artistId)
                .orElseThrow(() ->
                        new CustomException("해당 아티스트를 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
                );

        // 4) 팔로우 엔티티 생성 및 저장
        ArtistFollow af = ArtistFollow.builder()
                .user(user)
                .artist(artist)
                .createdAt(LocalDateTime.now())
                .build();
        artistFollowRepository.save(af);
    }

    @Override
    public Artist findById(Long artistId) {
        return artistRepository.findByArtistId(artistId)
                .orElseThrow(() -> new CustomException("아티스트를 찾을 수 없습니다", HttpStatus.NOT_FOUND));
    }

}
