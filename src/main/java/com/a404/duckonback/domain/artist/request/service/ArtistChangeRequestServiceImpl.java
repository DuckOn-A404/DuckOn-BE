package com.a404.duckonback.domain.artist.request.service;

import com.a404.duckonback.common.enums.RequestStatus;
import com.a404.duckonback.common.exception.CustomException;
import com.a404.duckonback.common.response.ErrorCode;
import com.a404.duckonback.domain.artist.artist.repository.ArtistRepository;
import com.a404.duckonback.domain.artist.emerging.repository.EmergingArtistRepository;
import com.a404.duckonback.domain.artist.request.dto.ArtistChangeRequestCreateRequestDTO;
import com.a404.duckonback.domain.artist.request.entity.ArtistChangeTargetType;
import com.a404.duckonback.domain.artist.request.entity.ArtistProfileChangeRequest;
import com.a404.duckonback.domain.artist.request.repository.ArtistChangeRequestRepository;
import com.a404.duckonback.domain.user.entity.User;
import com.a404.duckonback.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
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
