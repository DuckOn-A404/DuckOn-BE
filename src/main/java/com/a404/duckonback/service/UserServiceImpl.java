package com.a404.duckonback.service;

import com.a404.duckonback.dto.*;
import com.a404.duckonback.entity.Follow;
import com.a404.duckonback.entity.Penalty;
import com.a404.duckonback.entity.User;
import com.a404.duckonback.exception.CustomException;
import com.a404.duckonback.repository.UserRepository;
import com.a404.duckonback.repository.projection.UserBrief;
import com.a404.duckonback.util.Anonymizer;
import com.a404.duckonback.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.awt.print.Pageable;
import java.time.LocalDateTime;
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
    private final TokenBlacklistService tokenBlacklistService;
    private final JWTUtil jWTUtil;
    private final RedisTemplate<String, Object> redisTemplate;

    // --- 가벼운 규칙 상수 ---
    private static final int SIZE_DEFAULT = 10;
    private static final int MIN_FALLBACK = 3;

    private static final int SCORE_ROOM_USER = 100;  // 같은 아티스트 방 참여자
    private static final int SCORE_ARTIST_FAN = 50;  // 같은 아티스트 팔로워
    private static final int SCORE_RECENT_HOST = 40; // 최근 방 호스트

    private static final int LIMIT_ARTIST_FOLLOWERS = 30;
    private static final int LIMIT_RECENT_HOSTS = 10;
    private static final int REDIS_SAMPLE_PER_ROOM = 20; // 방당 최대 샘플 수

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
        return userRepository.existsByEmailAndDeletedFalse(email);
    }

    @Override
    public boolean isUserIdDuplicate(String userId) {
        return userRepository.existsByUserIdAndDeletedFalse(userId);
    }

    @Override
    public boolean isNicknameDuplicate(String nickname) {
        return userRepository.existsByNicknameAndDeletedFalse(nickname);
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

        boolean isSocial = user.getHasLocalCredential() != null;

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
                .socialLogin(isSocial)
                .penaltyList(pennaltyList)
                .roomList(roomList)
                .build();
    }

    @Override
    @Transactional
    public void deleteUser(User user, String refreshHeader) {
        final long now = System.currentTimeMillis();
        final Long uid = user.getId();

        // 1) 토큰 블랙리스트 (멱등 처리)
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            String authorization = attrs.getRequest().getHeader("Authorization");
            String accessToken = jWTUtil.normalizeIfValid(authorization);
            if (accessToken != null) {
                Date exp = jWTUtil.getClaims(accessToken).getExpiration();
                long ttl = exp.getTime() - now;
                if (ttl > 0) tokenBlacklistService.blacklist(accessToken, ttl);
            }
        }
        String refreshToken = jWTUtil.normalizeIfValid(refreshHeader);
        if (refreshToken != null) {
            Date exp = jWTUtil.getClaims(refreshToken).getExpiration();
            long ttl = exp.getTime() - now;
            if (ttl > 0) tokenBlacklistService.blacklist(refreshToken, ttl);
        }

        // 2) S3 프로필 이미지 삭제
        if (user.getImgUrl() != null) {
            try { s3Service.deleteFile(user.getImgUrl()); } catch (Exception ignore) {}
        }

        // 3) 개인정보 익명화 + 탈퇴 마킹
        user.setEmail(Anonymizer.email(uid));
        user.setUserId(Anonymizer.userId(uid));
        user.setNickname(Anonymizer.nickname());
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString())); // 임의 강한 패스워드로 교체
        user.setProvider(null);
        user.setProviderId(null);
        user.setHasLocalCredential(false);
        user.setImgUrl(null);
        user.setDeleted(true);
        user.setDeletedAt(LocalDateTime.now());

        // 4) 저장 (flush까지 해서 유니크 충돌 즉시 감지)
        userRepository.saveAndFlush(user);
    }

    @Override
    public UserInfoResponseDTO getUserInfo(String myUserId, String otherUserId) {
        if (myUserId == null) {
            throw new CustomException("사용자 ID가 제공되지 않았습니다", HttpStatus.BAD_REQUEST);
        }
        if (!userRepository.existsByUserIdAndDeletedFalse(otherUserId)) {
            throw new CustomException("사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
        }

        User user = userRepository.findByUserIdAndDeletedFalse(otherUserId);

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
        if (!userRepository.existsByUserIdAndDeletedFalse(userId)) {
            throw new CustomException("사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
        }

        User user = userRepository.findByUserIdAndDeletedFalse(userId);

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
        if (!userRepository.existsByUserIdAndDeletedFalse(userId)) {
            throw new CustomException("사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
        }

        User user = userRepository.findByUserIdAndDeletedFalse(userId);

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
        if (!userRepository.existsByUserIdAndDeletedFalse(otherUserId)) {
            throw new CustomException("팔로우할 사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
        }
        if (myUserId.equals(otherUserId)) {
            throw new CustomException("자기 자신은 팔로우할 수 없습니다.", HttpStatus.BAD_REQUEST);
        }
        if (followService.isFollowing(myUserId, otherUserId)) {
            throw new CustomException("이미 팔로우한 사용자입니다.", HttpStatus.CONFLICT);
        }

        User myUser = userRepository.findByUserIdAndDeletedFalse(myUserId);
        User otherUser = userRepository.findByUserIdAndDeletedFalse(otherUserId);


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
        User user = userRepository.findByUserIdAndDeletedFalse(userId);
        if (user == null) {
            throw new CustomException("사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
        }

        User otherUser = userRepository.findByUserIdAndDeletedFalse(otherUserId);
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
        User user = userRepository.findByUserIdAndDeletedFalse(userId);
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
        User user = userRepository.findByUserIdAndDeletedFalse(userId);
        if (user == null) {
            throw new CustomException("사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
        }

        return passwordEncoder.matches(inputPassword, user.getPassword());
    }

    @Override
    public User findActiveByEmail(String email) {
        User user = userRepository.findByEmailAndDeletedFalse(email);
        if (user == null) {
            throw new CustomException("존재하지 않는 사용자입니다.", HttpStatus.UNAUTHORIZED);
        }
        return user;
    }

    @Override
    public User findActiveByUserId(String userId) {
        User user = userRepository.findByUserIdAndDeletedFalse(userId);
        if (user == null) {
            throw new CustomException("존재하지 않는 사용자입니다.", HttpStatus.UNAUTHORIZED);
        }
        return user;
    }

    @Override
    @Transactional(readOnly = true)
    public RecommendUsersResponseDTO recommendUsers(String myUserId, Long artistId, int size, boolean includeReasons) {
        if (size <= 0) size = SIZE_DEFAULT;

        User me = userRepository.findByUserIdAndDeletedFalse(myUserId);
        if (me == null) throw new CustomException("사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);

        // 이미 팔로우/본인 제외를 위해 내 팔로잉 userId 집합 준비
        Set<String> myFollowingUserIds = Optional.ofNullable(me.getFollowing())
                .orElse(List.of())
                .stream()
                .map(f -> f.getFollowing().getUserId())
                .collect(Collectors.toSet());

        // 후보 점수 및 사유
        Map<String, Candidate> scores = new HashMap<>();

        // ---------- A) 같은 아티스트의 현재 방 참여자(REDIS) ----------
        if (artistId != null) {
            String roomsKey = "artist:" + artistId + ":rooms";
            Set<Object> roomIds = redisTemplate.opsForSet().members(roomsKey);
            if (roomIds != null) {
                for (Object roomIdObj : roomIds) {
                    String roomId = String.valueOf(roomIdObj);
                    String usersKey = "room:" + roomId + ":users";

                    // 랜덤 샘플링 (가볍게)
                    List<Object> sampled = redisTemplate.opsForSet()
                            .randomMembers(usersKey, REDIS_SAMPLE_PER_ROOM);
                    if (sampled == null) continue;

                    for (Object uidObj : sampled) {
                        String uid = String.valueOf(uidObj);
                        if (!uid.equals(myUserId)) {
                            add(scores, uid, SCORE_ROOM_USER,
                                    includeReasons ? "같은 아티스트 방 참여자" : null);
                        }
                    }
                }
            }
        }

        // ---------- B) 같은 아티스트 팔로워(MySQL, 프로젝션) ----------
        if (artistId != null) {
            var followerBriefs = userRepository.findArtistFollowersBrief(
                    artistId, PageRequest.of(0, LIMIT_ARTIST_FOLLOWERS));
            for (UserBrief b : followerBriefs) {
                if (!b.getUserId().equals(myUserId)) {
                    add(scores, b.getUserId(), SCORE_ARTIST_FAN,
                            includeReasons ? "같은 아티스트 팔로워" : null);
                }
            }
        }

        // ---------- C) 같은 아티스트 최근 방 호스트(MySQL, 프로젝션) ----------
        if (artistId != null) {
            LocalDateTime since = LocalDateTime.now().minusDays(7);
            var hostBriefs = userRepository.findRecentHostsBrief(
                    artistId, since, PageRequest.of(0, LIMIT_RECENT_HOSTS));
            for (UserBrief b : hostBriefs) {
                if (!b.getUserId().equals(myUserId)) {
                    add(scores, b.getUserId(), SCORE_RECENT_HOST,
                            includeReasons ? "같은 아티스트 최근 방 호스트" : null);
                }
            }
        }

        // 후보 집합 → 필터(본인/이미 팔로우) → 프로필 벌크 조회
        List<String> candidateIds = scores.keySet().stream()
                .filter(uid -> !uid.equals(myUserId) && !myFollowingUserIds.contains(uid))
                .toList();

        List<UserBrief> briefs = candidateIds.isEmpty()
                ? List.of()
                : userRepository.findBriefsByUserIdIn(candidateIds);

        // 점수 기반 정렬 + DTO 변환
        List<RecommendedUserDTO> list = briefs.stream()
                .map(b -> RecommendedUserDTO.builder()
                        .userId(b.getUserId())
                        .nickname(b.getNickname())
                        .imgUrl(b.getImgUrl())
                        .reasons(includeReasons ? new ArrayList<>(scores.get(b.getUserId()).reasons) : null)
                        .build()
                )
                .sorted((a, b) -> {
                    int sa = scores.get(a.getUserId()).score;
                    int sb = scores.get(b.getUserId()).score;
                    if (sa != sb) return Integer.compare(sb, sa);
                    return Optional.ofNullable(a.getNickname()).orElse("")
                            .compareTo(Optional.ofNullable(b.getNickname()).orElse(""));
                })
                .limit(size)
                .toList();

        // 최소 3명 보장(가능하면) - 가볍게 랜덤 보충
        if (list.size() < Math.min(size, MIN_FALLBACK)) {
            int need = Math.min(size, MIN_FALLBACK) - list.size();
            var page = PageRequest.of(0, 200);
            var pool = userRepository.findAll(page).getContent();
            Set<String> exists = list.stream().map(RecommendedUserDTO::getUserId).collect(Collectors.toSet());

            var extras = pool.stream()
                    .map(u -> u.getUserId())
                    .filter(uid -> !uid.equals(myUserId)
                            && !exists.contains(uid)
                            && !myFollowingUserIds.contains(uid))
                    .limit(need)
                    .collect(Collectors.toSet());

            if (!extras.isEmpty()) {
                var extraBriefs = userRepository.findBriefsByUserIdIn(extras);
                var extraDtos = extraBriefs.stream()
                        .map(b -> RecommendedUserDTO.builder()
                                .userId(b.getUserId())
                                .nickname(b.getNickname())
                                .imgUrl(b.getImgUrl())
                                .reasons(includeReasons ? List.of("랜덤 보충") : null)
                                .build())
                        .toList();

                var tmp = new ArrayList<>(list);
                tmp.addAll(extraDtos);
                list = tmp;
            }
        }

        return RecommendUsersResponseDTO.builder().users(list).build();
    }

    private void add(Map<String, Candidate> map, String userId, int score, String reason) {
        var c = map.computeIfAbsent(userId, k -> new Candidate());
        c.score += score;
        if (reason != null) c.reasons.add(reason);
    }

    private static class Candidate {
        int score = 0;
        List<String> reasons = new ArrayList<>();
    }

}
