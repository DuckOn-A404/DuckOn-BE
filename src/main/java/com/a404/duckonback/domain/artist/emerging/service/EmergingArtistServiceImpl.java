package com.a404.duckonback.domain.artist.emerging.service;

import com.a404.duckonback.common.dto.PageResponse;
import com.a404.duckonback.common.enums.SortOrder;
import com.a404.duckonback.common.exception.CustomException;
import com.a404.duckonback.common.response.ErrorCode;
import com.a404.duckonback.domain.artist.emerging.dto.EmergingArtistCreateRequestDTO;
import com.a404.duckonback.domain.artist.emerging.dto.EmergingArtistCreateResponseDTO;
import com.a404.duckonback.domain.artist.emerging.dto.EmergingArtistDetailResponseDTO;
import com.a404.duckonback.domain.artist.emerging.dto.EmergingArtistListResponseDTO;
import com.a404.duckonback.domain.artist.emerging.entity.EmergingArtist;
import com.a404.duckonback.domain.artist.emerging.entity.EmergingArtistSort;
import com.a404.duckonback.domain.artist.emerging.entity.EmergingArtistStatus;
import com.a404.duckonback.domain.artist.emerging.repository.EmergingArtistFollowRepository;
import com.a404.duckonback.domain.artist.emerging.repository.EmergingArtistRepository;
import com.a404.duckonback.domain.user.entity.User;
import com.a404.duckonback.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
public class EmergingArtistServiceImpl implements EmergingArtistService {
    private final EmergingArtistRepository emergingArtistRepository;
    private final UserRepository userRepository;
    private final EmergingArtistFollowRepository emergingArtistFollowRepository;

    @Override
    public EmergingArtistCreateResponseDTO create(Long userId, EmergingArtistCreateRequestDTO req) {
        // 1. 중복 검사
        boolean exists = emergingArtistRepository.existsByNameKrAndNameEnAndStatus(
                req.getNameKr().trim(),
                req.getNameEn().trim(),
                EmergingArtistStatus.ACTIVE
        );
        if(exists){
            throw new CustomException(ErrorCode.DUPLICATE_EMERGING_ARTIST);
        }

        // 2. 사용자 조회
        User user = userRepository.findByIdAndDeletedFalse(userId);
        if(user == null){
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        // 3. 라이징 아티스트 생성
        EmergingArtist emergingArtist = emergingArtistRepository.save(EmergingArtist.builder()
                .nameKr(req.getNameKr().trim())
                .nameEn(req.getNameEn().trim())
                .imgUrl(req.getImgUrl().trim())
                .createdBy(user)
                .build());

        return EmergingArtistCreateResponseDTO.builder()
                .emergingArtistId(emergingArtist.getEmergingArtistId())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<EmergingArtistListResponseDTO> getList(int page, int size, String sort, String order, String keyword) {
        int safeSize = Math.min(Math.max(size, 1), 100); // 페이지 크기는 1에서 100 사이로 제한
        int safePage = Math.max(page - 1, 0); // 페이지 번호는 0부터 시작

        EmergingArtistSort sortKey = EmergingArtistSort.parse(sort);
        SortOrder sortOrder = SortOrder.parse(order);
        String kw = (keyword != null) ? keyword.trim() : null;

        Pageable pageable = PageRequest.of(safePage, safeSize);
        Page<EmergingArtistListResponseDTO> pageResult = emergingArtistRepository.pageEmergingArtists(pageable, sortKey, sortOrder, kw);

        // 페이지 수 에러 처리
        int totalPages = pageResult.getTotalPages();
        if(safePage >= totalPages && totalPages > 0) {
            throw new CustomException(ErrorCode.EXCEED_TOTAL_PAGES);
        }

        return PageResponse.from1Base(pageResult);
    }

    @Override
    public EmergingArtistDetailResponseDTO getEmergingArtistDetail(Long emergingArtistId, Long userId) {
        EmergingArtist emergingArtist = emergingArtistRepository.findById(emergingArtistId)
                .orElseThrow(() -> new CustomException(ErrorCode.EMERGING_ARTIST_NOT_FOUND));

        boolean following = false;
        if(userId != null){
            following = emergingArtistFollowRepository.findByUser_IdAndEmergingArtist(userId, emergingArtist) != null;
        }

        long followersCount = emergingArtistFollowRepository.countByEmergingArtist(emergingArtist);

        return EmergingArtistDetailResponseDTO.builder()
                .emergingArtistId(emergingArtist.getEmergingArtistId())
                .createdAt(emergingArtist.getCreatedAt())
                .debutDate(emergingArtist.getDebutDate())
                .nameKr(emergingArtist.getNameKr())
                .nameEn(emergingArtist.getNameEn())
                .imgUrl(emergingArtist.getImgUrl())
                .status(emergingArtist.getStatus())
                .followerCount(followersCount)
                .following(following)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmergingArtistListResponseDTO> getRandomEmergingArtistList(int count) {
        if(count <= 0) throw new CustomException(ErrorCode.SIZE_NOT_VALID);

        int safeCount = Math.min(count, 20); // 최대 20개까지 조회 가능

        return emergingArtistRepository.findRandomActiveEmergingArtists(safeCount).stream()
                .map(v -> EmergingArtistListResponseDTO.builder()
                        .emergingArtistId(v.getEmergingArtistId())
                        .createdAt(v.getCreatedAt())
                        .debutDate(v.getDebutDate())
                        .nameKr(v.getNameKr())
                        .nameEn(v.getNameEn())
                        .imgUrl(v.getImgUrl())
                        .status(EmergingArtistStatus.valueOf(v.getStatus()))
                        .build()
                )
                .toList();
    }
}
