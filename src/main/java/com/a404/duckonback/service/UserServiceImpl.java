package com.a404.duckonback.service;

import com.a404.duckonback.dto.PenaltyDTO;
import com.a404.duckonback.dto.RoomDTO;
import com.a404.duckonback.dto.UserDetailInfoResponseDTO;
import com.a404.duckonback.dto.UserInfoResponseDTO;
import com.a404.duckonback.entity.Penalty;
import com.a404.duckonback.entity.Room;
import com.a404.duckonback.entity.User;
import com.a404.duckonback.exception.CustomException;
import com.a404.duckonback.repository.RoomRepository;
import com.a404.duckonback.repository.UserRepository;
import com.a404.duckonback.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PenaltyService penaltyService;
    private final FollowService followService;

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmailWithPenalties(email)
                .orElseThrow(() -> new CustomException("존재하지 않는 사용자입니다.", HttpStatus.UNAUTHORIZED));
    }

    @Override
    public User findByUserId(String userId) {
        return userRepository.findByUserIdWithPenalties(userId)
                .orElseThrow(() -> new CustomException("존재하지 않는 사용자입니다.", HttpStatus.UNAUTHORIZED));
    }

    @Override
    public void save(User user) { userRepository.save(user);}

    @Override
    public boolean isEmailDuplicate(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public boolean isUserIdDuplicate(String userId) {
        return userRepository.existsByUserId(userId);
    }

    @Override
    public boolean isNicknameDuplicate(String nickname) {
        return userRepository.existsByNickname(nickname);
    }

    @Override
//    @Transactional(readOnly = true)
    public UserDetailInfoResponseDTO getUserDetailInfo(String userId) {
        User user = userRepository.findUserDetailWithArtistFollows(userId)
                .orElseThrow(() -> new CustomException("사용자 없음", HttpStatus.NOT_FOUND));

        return toDTO(user);
    }

    private UserDetailInfoResponseDTO toDTO(User user) {
        if (user == null) {
            throw new CustomException("사용자를 찾을 수 없습니다", HttpStatus.NOT_FOUND);
        }

        List<Long> artistList = Optional.ofNullable(user.getArtistFollows())
                .orElse(List.of())
                .stream()
                .map(af -> af.getArtist() != null ? af.getArtist().getArtistId() : null)
                .filter(Objects::nonNull)
                .toList();

        List<RoomDTO> roomList = Optional.ofNullable(user.getRooms())
                .orElse(List.of())
                .stream()
                .map(RoomDTO::fromEntity)
                .toList();

        List<Penalty> penalties = penaltyService.getActivePenaltiesByUser(user.getId());
        List<PenaltyDTO> pennaltyList = penalties.stream()
                .map(penalty -> PenaltyDTO.builder()
                        .startAt(penalty.getStartAt())
                        .endAt(penalty.getEndAt())
                        .reason(penalty.getReason())
                        .penaltyType(penalty.getPenaltyType().toString())
                        .status(penalty.getStatus().toString())
                        .build())
                .toList();

        return UserDetailInfoResponseDTO.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .role(user.getRole().toString())
                .language(user.getLanguage())
                .imgUrl(user.getImgUrl())
                .artistList(artistList)
                .followingCount(Optional.ofNullable(user.getFollowing()).orElse(List.of()).size())
                .followerCount(Optional.ofNullable(user.getFollowers()).orElse(List.of()).size())
                .penaltyList(pennaltyList)
                .roomList(roomList)
                .build();
    }

    @Override
    @Transactional
    public void deleteUser(User user, String refreshToken) {
        userRepository.delete(user);
        //refreshToken 블랙리스트 등록
    }

    @Override
    public UserInfoResponseDTO getUserInfo(String myUserId, String otherUserId) {
        if (myUserId == null) {
            throw new CustomException("사용자 ID가 제공되지 않았습니다", HttpStatus.BAD_REQUEST);
        }
        if (!userRepository.existsByUserId(otherUserId)) {
            throw new CustomException("사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
        }

        User user = userRepository.findByUserId(otherUserId);

        return UserInfoResponseDTO.builder()
                .userId(user.getUserId())
                .nickname(user.getNickname())
                .imgUrl(user.getImgUrl())
                .followingCount(user.getFollowing().size())
                .followerCount(user.getFollowers().size())
                .following(followService.isFollowing(myUserId, otherUserId))
                .build();
    }


}
