package com.a404.duckonback.domain.artist.emerging.service;

import com.a404.duckonback.common.exception.CustomException;
import com.a404.duckonback.common.response.ErrorCode;
import com.a404.duckonback.domain.artist.emerging.entity.EmergingArtist;
import com.a404.duckonback.domain.artist.emerging.entity.EmergingArtistFollow;
import com.a404.duckonback.domain.artist.emerging.repository.EmergingArtistFollowRepository;
import com.a404.duckonback.domain.artist.emerging.repository.EmergingArtistRepository;
import com.a404.duckonback.domain.user.entity.User;
import com.a404.duckonback.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class EmergingArtistFollowServiceImpl implements EmergingArtistFollowService {
    private final EmergingArtistFollowRepository emergingArtistFollowRepository;
    private final EmergingArtistRepository emergingArtistRepository;
    private final UserRepository userRepository;

    @Override
    public void followEmergingArtist(Long userId, Long emergingArtistId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        EmergingArtist emergingArtist = emergingArtistRepository.findById(emergingArtistId)
                .orElseThrow(() -> new CustomException(ErrorCode.EMERGING_ARTIST_NOT_FOUND));

        EmergingArtistFollow emergingArtistFollow = EmergingArtistFollow.builder()
                .user(user)
                .emergingArtist(emergingArtist)
                .build();

        emergingArtistFollowRepository.save(emergingArtistFollow);
    }

    @Override
    public void unfollowEmergingArtist(Long userId, Long emergingArtistId) {
        // 1. 라이징 아티스트 존재 여부 확인
        EmergingArtist emergingArtist = emergingArtistRepository.findById(emergingArtistId)
                .orElseThrow(() -> new CustomException(ErrorCode.EMERGING_ARTIST_NOT_FOUND));

        // 2. 팔로우 관계 조회
        EmergingArtistFollow emergingArtistFollow = emergingArtistFollowRepository.findByUser_IdAndEmergingArtist(userId, emergingArtist);

        // 3. 팔로우 관계 존재 여부 확인
        if (emergingArtistFollow == null) {
            throw new CustomException(ErrorCode.EMERGING_ARTIST_FOLLOW_NOT_FOUND);
        }

        // 4. 팔로우 관계 삭제
        emergingArtistFollowRepository.deleteByUser_IdAndEmergingArtist(userId, emergingArtist);
    }
}
