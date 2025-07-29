package com.a404.duckonback.service;

import com.a404.duckonback.dto.FollowedArtistDTO;
import com.a404.duckonback.entity.Artist;
import com.a404.duckonback.entity.ArtistFollow;
import com.a404.duckonback.entity.ArtistFollowId;
import com.a404.duckonback.entity.User;
import com.a404.duckonback.exception.CustomException;
import com.a404.duckonback.repository.ArtistFollowRepository;
import com.a404.duckonback.repository.ArtistRepository;
import com.a404.duckonback.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArtistFollowServiceImpl implements ArtistFollowService {

    private final ArtistRepository artistRepository;
    private final UserRepository userRepository;
    private final ArtistFollowRepository artistFollowRepository;

    @Override
    public ArtistFollow createArtistFollow(ArtistFollow artistFollow) {
        return artistFollowRepository.save(artistFollow);
    }

    @Override
    public Optional<ArtistFollow> getArtistFollow(Long id, Long artistId) {
        return artistFollowRepository.findById(new ArtistFollowId(id, artistId));
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
    public Page<FollowedArtistDTO> getFollowedArtists(Long userId, Pageable pageable) {
        // 사용자 존재 확인
        if (userRepository.findById(userId) == null) {
            throw new CustomException("존재하지 않는 사용자입니다.", HttpStatus.NOT_FOUND);
        }
        // 페이징 조회 후 DTO로 매핑
        return artistFollowRepository.findByUser_Id(userId, pageable)
                .map(af -> new FollowedArtistDTO(
                        af.getArtist().getArtistId(),
                        af.getArtist().getNameEn(),
                        af.getArtist().getNameKr(),
                        af.getArtist().getDebutDate(),
                        af.getArtist().getImgUrl()
                ));
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
    @Transactional
    public void unfollowArtist(Long userId, Long artistId) {
        // 1) 사용자 존재 확인
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new CustomException("존재하지 않는 사용자입니다.", HttpStatus.NOT_FOUND);
        }

        // 2) 아티스트 존재 확인
        Artist artist = artistRepository.findById(artistId)
                .orElseThrow(() ->
                        new CustomException("해당 아티스트를 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
                );

        // 3) 팔로우 중인지 확인
        boolean following = artistFollowRepository.existsByUser_IdAndArtist_ArtistId(userId, artistId);
        if (!following) {
            throw new CustomException("팔로우한 아티스트가 아닙니다.", HttpStatus.BAD_REQUEST);
        }

        // 4) 삭제 (트랜잭션 안에서 실행되어야 함)
        artistFollowRepository.deleteByUser_IdAndArtist_ArtistId(userId, artistId);
    }

    @Override
    @Transactional
    public void updateArtistFollows(Long userId, List<Long> artistIds) {
        // 1) 사용자 검증
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new CustomException("존재하지 않는 사용자입니다.", HttpStatus.NOT_FOUND);
        }

        // 2) 요청된 artistIds 중에 DB에 없는 ID가 있는지 검증
        List<Long> notFound = artistIds.stream()
                .filter(id -> artistRepository.findById(id).isEmpty())
                .collect(Collectors.toList());
        if (!notFound.isEmpty()) {
            throw new CustomException(
                    "다음 아티스트를 찾을 수 없습니다: " + notFound, HttpStatus.NOT_FOUND
            );
        }

        // 3) 현재 팔로우 중인 아티스트 목록
        Set<Long> existing = artistFollowRepository.findByUser_Id(userId).stream()
                .map(af -> af.getArtist().getArtistId())
                .collect(Collectors.toSet());

        Set<Long> requested = Set.copyOf(artistIds);

        // 4) 새로 팔로우할 것들 = 요청 목록 - 기존
        Set<Long> toFollow = requested.stream()
                .filter(id -> !existing.contains(id))
                .collect(Collectors.toSet());

        // 5) 언팔로우할 것들 = 기존 - 요청
        Set<Long> toUnfollow = existing.stream()
                .filter(id -> !requested.contains(id))
                .collect(Collectors.toSet());

        // 6) 팔로우 추가
        for (Long aid : toFollow) {
            Artist artist = artistRepository.findById(aid).get(); // 이미 존재 검증됨
            artistFollowRepository.save(
                    com.a404.duckonback.entity.ArtistFollow.builder()
                            .user(user)
                            .artist(artist)
                            .createdAt(java.time.LocalDateTime.now())
                            .build()
            );
        }

        // 7) 팔로우 취소
        for (Long aid : toUnfollow) {
            artistFollowRepository.deleteByUser_IdAndArtist_ArtistId(userId, aid);
        }
    }

}
