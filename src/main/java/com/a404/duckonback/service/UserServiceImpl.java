package com.a404.duckonback.service;

import com.a404.duckonback.dto.*;
import com.a404.duckonback.entity.Follow;
import com.a404.duckonback.entity.Penalty;
import com.a404.duckonback.entity.User;
import com.a404.duckonback.exception.CustomException;
import com.a404.duckonback.repository.UserRepository;
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

    public User findByEmail(String email) {
        return userRepository.findByEmailWithPenalties(email)
                .orElseThrow(() -> new CustomException("존재하지 않는 사용자입니다.", HttpStatus.UNAUTHORIZED));
    }

    public User findByUserId(String userId) {
        return userRepository.findByUserIdWithPenalties(userId)
                .orElseThrow(() -> new CustomException("존재하지 않는 사용자입니다.", HttpStatus.UNAUTHORIZED));
    }

    public void save(User user) { userRepository.save(user);}


    public boolean isEmailDuplicate(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean isUserIdDuplicate(String userId) {
        return userRepository.existsByUserId(userId);
    }

    public boolean isNicknameDuplicate(String nickname) {
        return userRepository.existsByNickname(nickname);
    }


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

        List<Integer> artistList = Optional.ofNullable(user.getArtistFollows())
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


    @Override
    public FollowersResponseDTO getFollowers(String userId) {
        if (userId == null) {
            throw new CustomException("사용자 ID가 제공되지 않았습니다", HttpStatus.BAD_REQUEST);
        }
        if (!userRepository.existsByUserId(userId)) {
            throw new CustomException("사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
        }

        User user = userRepository.findByUserId(userId);

        List<Follow> followers = Optional.ofNullable(user.getFollowers()).orElse(List.of());
        List<FollowerInfoDTO> followerDTOs = followers.stream()
                .map(follower -> FollowerInfoDTO.builder()
                        .userId(follower.getFollower().getUserId())
                        .nickname(follower.getFollower().getNickname())
                        .profileImgUrl(follower.getFollower().getImgUrl())
                        .following(followService.isFollowing(userId, follower.getFollower().getUserId()))
                        .build())
                .toList();

        return FollowersResponseDTO.builder()
                .followers(followerDTOs)
                .build();
    }

    @Override
    public FollowingResponseDTO getFollowing(String userId) {
        if (userId == null) {
            throw new CustomException("사용자 ID가 제공되지 않았습니다", HttpStatus.BAD_REQUEST);
        }
        if (!userRepository.existsByUserId(userId)) {
            throw new CustomException("사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
        }

        User user = userRepository.findByUserId(userId);

        List<Follow> followings = Optional.ofNullable(user.getFollowing()).orElse(List.of());
        List<FollowingInfoDTO> followingDTOs = followings.stream()
                .map(following -> FollowingInfoDTO.builder()
                        .userId(following.getFollowing().getUserId())
                        .nickname(following.getFollowing().getNickname())
                        .profileImgUrl(following.getFollowing().getImgUrl())
                        .build())
                .toList();

        return FollowingResponseDTO.builder()
                .following(followingDTOs)
                .build();
    }

    @Override
    public void followUser(String myUserId, String otherUserId){
        if (myUserId == null || otherUserId == null) {
            throw new CustomException("사용자 ID가 제공되지 않았습니다", HttpStatus.BAD_REQUEST);
        }
        if (!userRepository.existsByUserId(otherUserId)) {
            throw new CustomException("팔로우할 사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
        }
        if (myUserId.equals(otherUserId)) {
            throw new CustomException("자기 자신은 팔로우할 수 없습니다.", HttpStatus.BAD_REQUEST);
        }
        if (followService.isFollowing(myUserId, otherUserId)) {
            throw new CustomException("이미 팔로우한 사용자입니다.", HttpStatus.CONFLICT);
        }

        User myUser = userRepository.findByUserId(myUserId);
        User otherUser = userRepository.findByUserId(otherUserId);


        Follow follow = followService.createFollow(Follow.builder()
                .follower(myUser)
                .following(otherUser)
                .createdAt(java.time.LocalDateTime.now())
                .build());

        followService.createFollow(follow);
    }

}
