package com.a404.duckonback.domain.artist.emerging.service;

import com.a404.duckonback.common.exception.CustomException;
import com.a404.duckonback.common.response.ErrorCode;
import com.a404.duckonback.domain.artist.emerging.dto.EmergingArtistCreateRequest;
import com.a404.duckonback.domain.artist.emerging.dto.EmergingArtistCreateResponse;
import com.a404.duckonback.domain.artist.emerging.entity.EmergingArtist;
import com.a404.duckonback.domain.artist.emerging.entity.EmergingArtistStatus;
import com.a404.duckonback.domain.artist.emerging.repository.EmergingArtistRepository;
import com.a404.duckonback.domain.user.entity.User;
import com.a404.duckonback.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmergingArtistServiceImpl implements EmergingArtistService {
    private final EmergingArtistRepository emergingArtistRepository;
    private final UserRepository userRepository;

    @Override
    public EmergingArtistCreateResponse create(Long userId, EmergingArtistCreateRequest req) {
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

        return EmergingArtistCreateResponse.builder()
                .emergingArtistId(emergingArtist.getEmergingArtistId())
                .build();
    }
}
