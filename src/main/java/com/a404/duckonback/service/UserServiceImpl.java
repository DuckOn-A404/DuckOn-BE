package com.a404.duckonback.service;

import com.a404.duckonback.dto.*;
import com.a404.duckonback.entity.Follow;
import com.a404.duckonback.entity.Penalty;
import com.a404.duckonback.entity.User;
import com.a404.duckonback.exception.CustomException;
import com.a404.duckonback.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PenaltyService penaltyService;
    private final FollowService followService;
    private final S3Service s3Service;

    private final PasswordEncoder passwordEncoder;

    private final RedisTemplate<String, Object> redisTemplate;

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

    @Override
    @Transactional
    public void unfollowUser(String userId, String otherUserId) {
        User user = userRepository.findByUserId(userId);
        if (user == null) {
            throw new CustomException("사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
        }

        User otherUser = userRepository.findByUserId(otherUserId);
        if (otherUser == null) {
            throw new CustomException("팔로우 취소할 사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
        }

        if( userId.equals(otherUserId)) {
            throw new CustomException("자기 자신은 팔로우 취소할 수 없습니다.", HttpStatus.BAD_REQUEST);
        }

        if (!followService.isFollowing(userId, otherUserId)) {
            throw new CustomException("팔로우하지 않은 사용자입니다.", HttpStatus.BAD_REQUEST);
        }

        followService.deleteFollow(userId, otherUserId);
    }

    @Override
    @Transactional
    public void updateUserInfo(String userId, UpdateProfileRequestDTO newUserInfo) {
        User user = userRepository.findByUserId(userId);
        if (user == null) {
            throw new CustomException("사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
        }

        if(newUserInfo == null) {
            throw new CustomException("새로운 사용자 정보가 제공되지 않았습니다.", HttpStatus.BAD_REQUEST);
        }

        if(newUserInfo.getNewPassword() != null && !newUserInfo.getNewPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(newUserInfo.getNewPassword()));
        }

        if(newUserInfo.getNickname() != null && !newUserInfo.getNickname().isBlank()) {
            user.setNickname(newUserInfo.getNickname());
        }

        if(newUserInfo.getLanguage() != null && !newUserInfo.getLanguage().isBlank()) {
            user.setLanguage(newUserInfo.getLanguage());
        }

        MultipartFile file = newUserInfo.getProfileImg();
        if (file != null && !file.isEmpty()) {
            // 이전 이미지 삭제
            if(user.getImgUrl() != null && !user.getImgUrl().isBlank()) {
                s3Service.deleteFile(user.getImgUrl());
            }
            String newImgUrl = s3Service.uploadFile(file);
            user.setImgUrl(newImgUrl);
        } else {
            user.setImgUrl(null);
        }
    }
    @Override
    public boolean verifyPassword(String userId, String inputPassword){
        User user = userRepository.findByUserId(userId);
        if (user == null) {
            throw new CustomException("사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
        }

        return passwordEncoder.matches(inputPassword, user.getPassword());
    }

    @Override
    @Transactional(readOnly = true)
    public RecommendUsersResponseDTO recommendUsers(String myUserId, Long artistId, int size, boolean includeReasons) {
        if (size <= 0) size = 10;

        User me = userRepository.findByUserId(myUserId);
        if (me == null) {
            throw new CustomException("사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
        }

        // 내가 팔로우 중인 사람 id(빅인트 PK) 집합 (겹치는 팔로우 계산용)
        var myFollowingIds = Optional.ofNullable(me.getFollowing())
                .orElse(List.of())
                .stream()
                .map(f -> f.getFollowing().getId())
                .collect(Collectors.toSet());

        // 후보 점수/사유 누적
        Map<String, Candidate> candidates = new HashMap<>();

        // 1) 같은 아티스트 현재 방 참여자 (Redis)
        if (artistId != null) {
            String roomsKey = "artist:" + artistId + ":rooms"; // Redis 설계에 이미 존재
            Set<Object> roomIds = redisTemplate.opsForSet().members(roomsKey);
            if (roomIds != null) {
                for (Object roomIdObj : roomIds) {
                    String roomId = String.valueOf(roomIdObj);
                    String usersKey = "room:" + roomId + ":users";
                    Set<Object> users = redisTemplate.opsForSet().members(usersKey);
                    if (users != null) {
                        for (Object uidObj : users) {
                            String uid = String.valueOf(uidObj);
                            if (!uid.equals(myUserId)) {
                                addScore(candidates, uid, 100, includeReasons ? "같은 아티스트 방 참여자" : null);
                            }
                        }
                    }
                }
            }
        }

        // 2) 같은 아티스트 팔로워 (MySQL)
        if (artistId != null) {
            for (User u : userRepository.findUsersFollowingArtist(artistId)) {
                if (!u.getUserId().equals(myUserId)) {
                    addScore(candidates, u.getUserId(), 50, includeReasons ? "같은 아티스트 팔로워" : null);
                }
            }
        }

        // 3) 같은 아티스트 최근 방 호스트 (MySQL)
        if (artistId != null) {
            for (User u : userRepository.findRecentRoomCreatorsByArtist(artistId)) {
                if (!u.getUserId().equals(myUserId)) {
                    addScore(candidates, u.getUserId(), 40, includeReasons ? "같은 아티스트 최근 방 호스트" : null);
                }
            }
        }

        // 4) 친구의 친구 (2-hop)
        for (User u : userRepository.findFriendsOfFriends(myUserId)) {
            if (!u.getUserId().equals(myUserId)) {
                addScore(candidates, u.getUserId(), 30, includeReasons ? "친구의 친구" : null);
            }
        }

        // 이미 팔로우한 계정/본인 제외
        Set<String> myFollowingUserIds = Optional.ofNullable(me.getFollowing())
                .orElse(List.of())
                .stream()
                .map(f -> f.getFollowing().getUserId())
                .collect(Collectors.toSet());

        // DTO 변환(+ mutualFollows 가산 점수)
        List<RecommendedUserDTO> list = candidates.keySet().stream()
                .filter(uid -> !uid.equals(myUserId) && !myFollowingUserIds.contains(uid))
                .map(uid -> {
                    User u = userRepository.findByUserId(uid);
                    if (u == null) return null;

                    // mutual follows 계산 (내 팔로잉 ∩ 상대 팔로잉)
                    Set<Long> candidateFollowingIds = Optional.ofNullable(u.getFollowing())
                            .orElse(List.of())
                            .stream()
                            .map(f -> f.getFollowing().getId())
                            .collect(Collectors.toSet());

                    int mutual = 0;
                    for (Long id : candidateFollowingIds) {
                        if (myFollowingIds.contains(id)) mutual++;
                    }

                    // 최종 점수 = baseScore + mutual*10
                    Candidate c = candidates.get(uid);
                    c.totalScore += (mutual * 10);

                    return RecommendedUserDTO.builder()
                            .userId(u.getUserId())
                            .nickname(u.getNickname())
                            .imgUrl(u.getImgUrl())
                            .mutualFollows(mutual)
                            .reasons(includeReasons ? new ArrayList<>(c.reasons) : null)
                            .build();
                })
                .filter(Objects::nonNull)
                .sorted((a, b) -> {
                    // 점수 내림차순, 같은 점수면 닉네임 ASC
                    int scoreA = candidates.get(a.getUserId()).totalScore;
                    int scoreB = candidates.get(b.getUserId()).totalScore;
                    if (scoreA != scoreB) return Integer.compare(scoreB, scoreA);
                    return Optional.ofNullable(a.getNickname()).orElse("")
                            .compareTo(Optional.ofNullable(b.getNickname()).orElse(""));
                })
                .limit(size)
                .toList();

        // 최소 3명 보장(가능하면)
        if (list.size() < Math.min(size, 3)) {
            int need = Math.min(size, 3) - list.size();

            // 랜덤 보충: 전체 유저에서 나/이미 포함/이미 팔로우 제외
            var page = org.springframework.data.domain.PageRequest.of(0, 200);
            var pool = userRepository.findAll(page).getContent();
            var exists = list.stream().map(RecommendedUserDTO::getUserId).collect(Collectors.toSet());
            var extras = pool.stream()
                    .filter(u -> !u.getUserId().equals(myUserId) && !exists.contains(u.getUserId()) && !myFollowingUserIds.contains(u.getUserId()))
                    .limit(need)
                    .map(u -> RecommendedUserDTO.builder()
                            .userId(u.getUserId())
                            .nickname(u.getNickname())
                            .imgUrl(u.getImgUrl())
                            .mutualFollows(0)
                            .reasons(includeReasons ? List.of("랜덤 보충") : null)
                            .build())
                    .toList();

            list = new java.util.ArrayList<>(list);
            list.addAll(extras);
        }

        return RecommendUsersResponseDTO.builder().users(list).build();
    }

    private void addScore(Map<String, Candidate> map, String userId, int score, String reasonOrNull) {
        Candidate c = map.get(userId);
        if (c == null) {
            c = new Candidate();
            map.put(userId, c);
        }
        c.totalScore = Math.max(c.totalScore, 0) + score; // 누적
        if (reasonOrNull != null) c.reasons.add(reasonOrNull);
    }

    private static class Candidate {
        int totalScore = 0;
        List<String> reasons = new ArrayList<>();
    }


}
