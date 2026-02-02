package com.a404.duckonback.domain.artist.request.service;

import com.a404.duckonback.common.dto.PageResponse;
import com.a404.duckonback.common.enums.RequestStatus;
import com.a404.duckonback.common.exception.CustomException;
import com.a404.duckonback.common.response.ErrorCode;
import com.a404.duckonback.domain.artist.artist.repository.ArtistRepository;
import com.a404.duckonback.domain.artist.emerging.repository.EmergingArtistRepository;
import com.a404.duckonback.domain.artist.request.dto.ArtistChangeRequestCreateRequestDTO;
import com.a404.duckonback.domain.artist.request.dto.ArtistChangeRequestInfoDTO;
import com.a404.duckonback.domain.artist.request.entity.ArtistChangeTargetType;
import com.a404.duckonback.domain.artist.request.entity.ArtistProfileChangeRequest;
import com.a404.duckonback.domain.artist.request.repository.ArtistChangeRequestRepository;
import com.a404.duckonback.domain.user.entity.User;
import com.a404.duckonback.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ArtistChangeRequestServiceImpl implements ArtistChangeRequestService {
    private final ArtistChangeRequestRepository artistChangeRequestRepository;
    private final UserRepository userRepository;
    private final ArtistRepository artistRepository;
    private final EmergingArtistRepository emergingArtistRepository;

    @Override
    public void create(Long userId, ArtistChangeRequestCreateRequestDTO req) {
        // 1. 사용자 존재 여부 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 2. 대상 아티스트 또는 신인 아티스트 존재 여부 확인
        validateTargetExists(req.getTargetType(), req.getTargetId());

        // 4. 아티스트 정보 변경 요청 생성 및 저장
        artistChangeRequestRepository.save(ArtistProfileChangeRequest.builder()
                .requestedBy(user)
                .targetType(req.getTargetType())
                .targetId(req.getTargetId())
                .status(RequestStatus.PENDING)
                .content(req.getContent().trim())
                .attachment(req.getAttachment() == null ? null : req.getAttachment().trim())
                .build());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ArtistChangeRequestInfoDTO> getMyRequests(int page, int size, long userId) {
        // 1. 사용자 존재 여부 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 2. 페이징 처리된 아티스트 정보 변경 요청 조회
        int safePage = Math.max(page - 1, 0); // 페이지 번호는 0부터 시작
        int safeSize = Math.min(Math.max(size, 1), 100); // 페이지 크기는 최소 1 이상

        Pageable pageable = PageRequest.of(safePage, safeSize);
        Page<ArtistProfileChangeRequest> pageResult = artistChangeRequestRepository.findByRequestedBy_IdOrderByCreatedAtDesc(userId, pageable);

        int totalPages = pageResult.getTotalPages();
        if(safePage >= totalPages && totalPages > 0) {
            throw new CustomException(ErrorCode.EXCEED_TOTAL_PAGES);
        }

        Page<ArtistChangeRequestInfoDTO> dtoPage = pageResult.map(ArtistChangeRequestInfoDTO::fromEntity);

        return PageResponse.from1Base(dtoPage);
    }

    private void validateTargetExists(ArtistChangeTargetType type, Long targetId) {
        if (type == null || targetId == null) {
            throw new CustomException(ErrorCode.ARTIST_INFO_EMPTY);
        }

        boolean exists;
        if (type == ArtistChangeTargetType.ARTIST) {
            exists = artistRepository.existsById(targetId);
            if (!exists) throw new CustomException(ErrorCode.ARTIST_NOT_FOUND);
        } else if (type == ArtistChangeTargetType.EMERGING_ARTIST) {
            exists = emergingArtistRepository.existsById(targetId);
            if (!exists) throw new CustomException(ErrorCode.EMERGING_ARTIST_NOT_FOUND);
        } else {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }
    }
}
