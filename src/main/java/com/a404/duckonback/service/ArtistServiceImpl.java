package com.a404.duckonback.service;

import com.a404.duckonback.dto.AdminArtistPatchDTO;
import com.a404.duckonback.dto.AdminArtistRequestDTO;
import com.a404.duckonback.dto.ArtistDTO;
import com.a404.duckonback.dto.ArtistDetailDTO;
import com.a404.duckonback.entity.Artist;
import com.a404.duckonback.entity.ArtistFollow;
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
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArtistServiceImpl implements ArtistService {

    private final ArtistRepository artistRepository;
    private final UserRepository userRepository;
    private final ArtistFollowRepository artistFollowRepository;
    private final S3Service s3Service;

    @Override
    public Artist findById(Long artistId) {
        return artistRepository.findByArtistId(artistId)
                .orElseThrow(() -> new CustomException("아티스트를 찾을 수 없습니다", HttpStatus.NOT_FOUND));
    }

    @Override
    public List<Long> findAllArtistIdByUserId(Long id){
        return artistRepository.findAllArtistIdByUserId(id);
    }

    @Override
    public ArtistDetailDTO getArtistDetail(Long userId, Long artistId) {
        Artist artist = artistRepository.findById(artistId)
                .orElseThrow(() -> new CustomException(
                        "해당 아티스트를 찾을 수 없습니다. ID: " + artistId,
                        HttpStatus.NOT_FOUND
                ));

        // 로그인 유저 정보가 없으면 (비로그인) userId == null 일 수 있으므로
        boolean isFollowed = false;
        if (userId != null && userRepository.findById(userId) != null) {
            isFollowed = artistFollowRepository
                    .existsByUser_IdAndArtist_ArtistId(userId, artistId);
        }

        return ArtistDetailDTO.of(artist, isFollowed);
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
    public Artist createArtist(AdminArtistRequestDTO dto) {
        // 1) 중복 검사
        if (artistRepository.existsByNameEnOrNameKr(dto.getNameEn(), dto.getNameKr())) {
            throw new CustomException("이미 존재하는 아티스트입니다.", HttpStatus.CONFLICT);
        }

        // 2) debutDate 기본값
        LocalDate debut = dto.getDebutDate() != null
                ? dto.getDebutDate()
                : LocalDate.now();

        // 3) 이미지 업로드 (선택)
        String imgUrl = null;
        MultipartFile file = dto.getImage();
        if (file != null && !file.isEmpty()) {
            // artist/{nameEn}/... 경로 아래 저장
            String prefix = String.format("artist/%s", dto.getNameEn());
            imgUrl = s3Service.uploadFile(file, prefix);
        }

        // 4) 엔티티 생성 및 저장
        Artist artist = Artist.builder()
                .nameEn(dto.getNameEn())
                .nameKr(dto.getNameKr())
                .debutDate(debut)
                .imgUrl(imgUrl)
                .build();
        return artistRepository.save(artist);
    }

    @Override
    public Artist updateArtist(Long artistId, AdminArtistRequestDTO dto) {
        // 1) 기존 아티스트 조회
        Artist artist = artistRepository.findById(artistId)
                .orElseThrow(() -> new CustomException("아티스트를 찾을 수 없습니다. ID: " + artistId,
                        HttpStatus.NOT_FOUND));

        // 2) 이름 중복 검사 (다른 레코드에 같은 영문/한글명이 있는지)
        Optional<Artist> conflict = artistRepository
                .findByNameEnOrNameKr(dto.getNameEn(), dto.getNameKr())
                .filter(a -> !a.getArtistId().equals(artistId));
        if (conflict.isPresent()) {
            throw new CustomException("이미 존재하는 아티스트 이름입니다.", HttpStatus.CONFLICT);
        }

        // 3) debutDate 갱신
        LocalDate debut = dto.getDebutDate() != null
                ? dto.getDebutDate()
                : artist.getDebutDate();
        artist.setDebutDate(debut);

        // 4) 이름 갱신
        artist.setNameEn(dto.getNameEn());
        artist.setNameKr(dto.getNameKr());

        // 5) 이미지 파일 처리
        MultipartFile file = dto.getImage();
        if (file != null && !file.isEmpty()) {
            // 기존 이미지가 있으면 삭제
            if (artist.getImgUrl() != null) {
                s3Service.deleteFile(artist.getImgUrl());
            }
            // 새로 업로드 (artist/{nameEn}/... 경로)
            String prefix = String.format("artist/%s", dto.getNameEn());
            String newUrl = s3Service.uploadFile(file, prefix);
            artist.setImgUrl(newUrl);
        }

        // 6) 저장
        return artistRepository.save(artist);
    }

    @Override
    public Artist patchArtist(Long artistId, AdminArtistPatchDTO dto) {
        Artist artist = artistRepository.findById(artistId)
                .orElseThrow(() -> new CustomException("아티스트를 찾을 수 없습니다. ID: " + artistId,
                        HttpStatus.NOT_FOUND));

        // 이름 중복 검사 (값이 들어왔을 때만)
        if (dto.getNameEn() != null || dto.getNameKr() != null) {
            Optional<Artist> conflict = artistRepository.findByNameEnOrNameKr(
                    dto.getNameEn() != null ? dto.getNameEn() : artist.getNameEn(),
                    dto.getNameKr() != null ? dto.getNameKr() : artist.getNameKr()
            ).filter(a -> !a.getArtistId().equals(artistId));
            if (conflict.isPresent()) {
                throw new CustomException("이미 존재하는 아티스트 이름입니다.", HttpStatus.CONFLICT);
            }
        }

        // 각 필드별 patch
        if (dto.getDebutDate() != null) {
            artist.setDebutDate(dto.getDebutDate());
        }
        if (dto.getNameEn() != null) {
            artist.setNameEn(dto.getNameEn());
        }
        if (dto.getNameKr() != null) {
            artist.setNameKr(dto.getNameKr());
        }
        if (dto.getImage() != null && !dto.getImage().isEmpty()) {
            // 기존 이미지 삭제
            if (artist.getImgUrl() != null) {
                s3Service.deleteFile(artist.getImgUrl());
            }
            String prefix = String.format("artist/%s", artist.getNameEn());
            String newUrl = s3Service.uploadFile(dto.getImage(), prefix);
            artist.setImgUrl(newUrl);
        }

        return artistRepository.save(artist);
    }


}
