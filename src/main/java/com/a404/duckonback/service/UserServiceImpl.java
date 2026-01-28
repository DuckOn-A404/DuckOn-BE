package com.a404.duckonback.service;

import com.a404.duckonback.dto.*;
import com.a404.duckonback.entity.Follow;
import com.a404.duckonback.entity.Meme;
import com.a404.duckonback.entity.Penalty;
import com.a404.duckonback.entity.User;
import com.a404.duckonback.enums.SocialProvider;
import com.a404.duckonback.exception.CustomException;
import com.a404.duckonback.repository.MemeRepository;
import com.a404.duckonback.repository.RoomRepository;
import com.a404.duckonback.repository.UserRepository;
import com.a404.duckonback.repository.projection.UserBrief;
import com.a404.duckonback.response.ErrorCode;
import com.a404.duckonback.util.Anonymizer;
import com.a404.duckonback.util.JWTUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions; // >>> CHANGED
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets; // >>> CHANGED
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final RedisService redisService;
    private final PenaltyService penaltyService;
    private final FollowService followService;
    private final S3Service s3Service;
    private final PasswordEncoder passwordEncoder;
    private final TokenBlacklistService tokenBlacklistService;
    private final JWTUtil jWTUtil;
    private final RedisTemplate<String, Object> redisTemplate;
    private final UserRankService userRankService;
    private final MemeRepository memoRepository;

    // --- 가벼운 규칙 상수 ---
    private static final int SIZE_DEFAULT = 10;
    private static final int MIN_FALLBACK = 3;

    private static final int SCORE_ROOM_USER = 100;  // 같은 아티스트 방 참여자
    private static final int SCORE_ARTIST_FAN = 50;  // 같은 아티스트 팔로워
    private static final int SCORE_RECENT_HOST = 40; // 최근 방 호스트

    private static final int LIMIT_ARTIST_FOLLOWERS = 30;
    private static final int LIMIT_RECENT_HOSTS = 10;
    private static final int REDIS_SAMPLE_PER_ROOM = 20; // 방당 최대 샘플 수

    // >>> CHANGED: room 키 프리픽스(현재 Redis 스키마에 맞춤)
    private static final String ROOM_KEY_PREFIX = "room:";
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final MemeRepository memeRepository;

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
    public UserDetailInfoResponseDTO getUserDetailInfo(String userId) {
        User user = userRepository.findUserDetailWithArtistFollows(userId)
                .orElseThrow(() -> new CustomException("사용자 없음", HttpStatus.NOT_FOUND));

        return toDTO(user);
    }

    @Override
    public RoomSummaryPageDTO getUserRoomCreateHistory(String userId, int page, int size) {
        // 1. 유저
        User user = userRepository.findByUserIdAndDeletedFalse(userId);
        if(user == null){
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        // 2. 페이지, 사이즈 보정
        int safePage = Math.max(1, page);
        int safeSize = Math.max(1, size);
        Pageable pageable = PageRequest.of(safePage - 1, safeSize);

        // 3. 방 목록 + count 조회
        Page<RoomSummaryDTO> roomPage = roomRepository.findRoomSummariesByCreatorIdOrderByCreatedAtDesc(user.getId(), pageable);
        List<RoomSummaryDTO> content = roomPage.getContent();

        // 4. 1페이지일 때만 Redis에서 현재 라이브 방 조회 후 active 표시
        if(safePage == 1 && !content.isEmpty()){
            RoomListInfoDTO activeRoom = redisService.getActiveRoomByHost(userId);

            if(activeRoom != null && activeRoom.getRoomId() != null){
                Long activeRoomId = activeRoom.getRoomId();

                // 첫 번째 히스토리가 active 방이면 true로 설정
                RoomSummaryDTO first = content.getFirst();
                if(activeRoomId.equals(first.getRoomId())){
                    first.setActive(true);
                }
            }
        }

        return RoomSummaryPageDTO.builder()
                .page(safePage)
                .size(safeSize)
                .total((int) roomPage.getTotalElements())
                .roomList(roomPage.getContent())
                .build();
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
                .userRank(userRankService.getUserRank(user.getId()))
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
    @Transactional(readOnly = true)
    public UserInfoResponseDTO getUserInfo(String myUserId, String otherUserId) {
        if (myUserId == null) throw new CustomException("사용자 ID가 제공되지 않았습니다", HttpStatus.BAD_REQUEST);
        if (!userRepository.existsByUserIdAndDeletedFalse(otherUserId))
            throw new CustomException("사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);

        User user = userRepository.findByUserIdAndDeletedFalse(otherUserId);

        Page<RoomSummaryDTO> roomPage = roomRepository.findRoomSummariesByCreatorIdOrderByCreatedAtDesc(
                user.getId(), PageRequest.of(0, 10)
        );

        // 현재 라이브(레디스)
        RoomListInfoDTO active = redisService.getActiveRoomByHost(otherUserId);

        // 랭킹
        UserRankDTO rankDTO = userRankService.getUserRank(user.getId());

        return UserInfoResponseDTO.builder()
                .userId(user.getUserId())
                .nickname(user.getNickname())
                .imgUrl(user.getImgUrl())
                .followingCount(Optional.ofNullable(user.getFollowing()).orElse(List.of()).size())
                .followerCount(Optional.ofNullable(user.getFollowers()).orElse(List.of()).size())
                .following(followService.isFollowing(myUserId, otherUserId))
                .roomList(roomPage.getContent())
                .activeRoom(active)
                .userRank(rankDTO)
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
                .map(Follow::getFollower)
                .filter(f -> f != null && !f.isDeleted())
                .map(f -> FollowerInfoDTO.builder()
                        .userId(f.getUserId())
                        .nickname(f.getNickname())
                        .profileImgUrl(f.getImgUrl())
                        .following(followService.isFollowing(userId, f.getUserId()))
                        .userRankDTO(userRankService.getUserRank(f.getId()))
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
                .map(Follow::getFollowing)
                .filter(f -> f != null && !f.isDeleted())
                .map(f -> FollowingInfoDTO.builder()
                        .userId(f.getUserId())
                        .nickname(f.getNickname())
                        .profileImgUrl(f.getImgUrl())
                        .userRank(userRankService.getUserRank(f.getId()))
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

        if(newUserInfo.getNickname() != null && !newUserInfo.getNickname().isBlank()) {
            user.setNickname(newUserInfo.getNickname());
        }

        if(newUserInfo.getLanguage() != null && !newUserInfo.getLanguage().isBlank()) {
            user.setLanguage(newUserInfo.getLanguage());
        }

        // TODO 기본이미지 변경부분 바꾸기
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
    public Optional<User> findActiveByProviderAndProviderId(SocialProvider provider, String providerId) {
        return userRepository.findByProviderAndProviderIdAndDeletedFalse(provider, providerId);
    }

    @Override
    @Transactional(readOnly = true)
    public RecommendUsersResponseDTO recommendUsers(String myUserId, Long artistId, int size, boolean includeReasons) {
        if (size <= 0) size = SIZE_DEFAULT;

        // 1) 로그인/게스트 분기
        final User me = (myUserId != null)
                ? userRepository.findByUserIdAndDeletedFalse(myUserId)
                : null;

        if (myUserId != null && me == null) {
            // 로그인 상태인데 사용자 없으면 예외
            throw new CustomException("사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
        }

        // >>> CHANGED: null-안전 팔로잉 집합
        final Set<String> myFollowingUserIds = (me != null)
                ? Optional.ofNullable(me.getFollowing()).orElse(List.of())
                  .stream()
                  .map(f -> f != null ? f.getFollowing() : null)
                  .filter(Objects::nonNull)
                  .map(u -> u.getUserId())
                  .filter(Objects::nonNull)
                  .collect(Collectors.toSet())
                : Collections.emptySet();

        // 2) 점수 집계
        Map<String, Candidate> scores = new HashMap<>();

        // A) 같은 아티스트 현재 방 참여자 (Redis)  >>> CHANGED: 인덱스 세트 대신 SCAN + DTO 필터
        if (artistId != null) {
            Set<String> roomIds = findRoomIdsByArtist(artistId); // >>> CHANGED

            for (String roomId : roomIds) {
                String usersKey = roomUsersKey(roomId);

                // [수정] 기본 JSON 번역기(Serializer)를 우회하여 원본 데이터 직접 가져오기
                List<String> sampledUsers = redisTemplate.execute((RedisCallback<List<String>>) connection -> {
                    RedisSerializer<String> keySerializer = (RedisSerializer<String>) redisTemplate.getKeySerializer();
                    byte[] keyBytes = keySerializer.serialize(usersKey);
                    // SRANDMEMBER: Set에서 무작위 멤버를 가져오는 Redis 원본 명령어
                    List<byte[]> resultBytes = connection.setCommands().sRandMember(keyBytes, REDIS_SAMPLE_PER_ROOM);
                    List<String> results = new ArrayList<>();
                    if (resultBytes != null) {
                        for (byte[] bytes : resultBytes) {
                            if (bytes != null) results.add(new String(bytes, StandardCharsets.UTF_8));
                        }
                    }
                    return results;
                });

                if (sampledUsers == null) continue;

                for (String uidStr : sampledUsers) {
                    String uid = null;
                    // 이제 안전하게 가져온 문자열이 JSON인지 일반 텍스트인지 우리 코드에서 직접 판별
                    if (uidStr.trim().startsWith("{")) {
                        try {
                            Map<String, Object> m = objectMapper.readValue(uidStr, Map.class);
                            uid = (String) m.get("userId");
                        } catch (Exception ignore) {}
                    } else {
                        uid = uidStr; // 옛날 데이터 형식
                    }

                    if (uid != null && (myUserId == null || !uid.equals(myUserId))) {
                        add(scores, uid, SCORE_ROOM_USER, includeReasons ? "같은 아티스트 방 참여자" : null);
                    }
                }
            }
        }

        // B) 같은 아티스트 팔로워 (MySQL, 프로젝션)
        if (artistId != null) {
            var followerBriefs = userRepository.findArtistFollowersBrief(
                    artistId, PageRequest.of(0, LIMIT_ARTIST_FOLLOWERS));
            for (UserBrief b : followerBriefs) {
                if (myUserId == null || !b.getUserId().equals(myUserId)) {
                    add(scores, b.getUserId(), SCORE_ARTIST_FAN, includeReasons ? "같은 아티스트 팔로워" : null);
                }
            }
        }

        // C) 같은 아티스트 최근 방 호스트 (MySQL, 프로젝션)
        if (artistId != null) {
            LocalDateTime since = LocalDateTime.now().minusDays(7);
            var hostBriefs = userRepository.findRecentHostsBrief(
                    artistId, since, PageRequest.of(0, LIMIT_RECENT_HOSTS));
            for (UserBrief b : hostBriefs) {
                if (myUserId == null || !b.getUserId().equals(myUserId)) {
                    add(scores, b.getUserId(), SCORE_RECENT_HOST, includeReasons ? "같은 아티스트 최근 방 호스트" : null);
                }
            }
        }

        // 3) 후보 → (로그인 시) 본인/이미 팔로우 제외
        List<String> candidateIds = scores.keySet().stream()
                .filter(uid -> {
                    if (myUserId != null && uid.equals(myUserId)) return false;  // 본인 제외
                    if (myFollowingUserIds.contains(uid)) return false;          // 팔로우 제외
                    return true;                                                 // 게스트는 전부 통과
                })
                .toList();

        List<UserBrief> briefs = candidateIds.isEmpty()
                ? List.of()
                : userRepository.findBriefsByIdIn(candidateIds);

        // 4) 정렬 및 DTO 변환  >>> CHANGED: 안전 가드(safeCand)로 NPE 방지
        List<RecommendedUserDTO> list = briefs.stream()
                .map(b -> {
                    Candidate c = safeCand(scores, b.getUserId());
                    return RecommendedUserDTO.builder()
                            .userId(b.getUserId())
                            .nickname(b.getNickname())
                            .imgUrl(b.getImgUrl())
                            .reasons(includeReasons ? new ArrayList<>(c.reasons) : null)
                            .userRank(userRankService.getUserRank(b.getId()))
                            .build();
                })
                .sorted((a, b) -> {
                    int sa = safeCand(scores, a.getUserId()).score;
                    int sb = safeCand(scores, b.getUserId()).score;
                    if (sa != sb) return Integer.compare(sb, sa);
                    return Optional.ofNullable(a.getNickname()).orElse("")
                            .compareTo(Optional.ofNullable(b.getNickname()).orElse(""));
                })
                .limit(size)
                .toList();

        // 5) 최소 3명 보장(가능하면) - 랜덤 보충 (게스트에도 동작)
        int needMin = Math.min(size, MIN_FALLBACK);
        if (list.size() < needMin) {
            int need = needMin - list.size();

            var page = PageRequest.of(0, 200);
            var pool = userRepository.findAllByDeletedFalse(page).getContent();

            Set<String> exists = list.stream().map(RecommendedUserDTO::getUserId).collect(Collectors.toSet());

            var extrasIds = pool.stream()
                    .map(User::getUserId)
                    .filter(uid -> {
                        if (myUserId != null && uid.equals(myUserId)) return false; // 본인 제외
                        if (exists.contains(uid)) return false;                     // 이미 담긴 후보 제외
                        if (myFollowingUserIds.contains(uid)) return false;         // 팔로잉 제외
                        return true;                                                // 게스트는 통과
                    })
                    .limit(need)
                    .collect(Collectors.toSet());

            if (!extrasIds.isEmpty()) {
                var extraBriefs = userRepository.findBriefsByIdIn(extrasIds);
                var extraDtos = extraBriefs.stream()
                        .map(b -> RecommendedUserDTO.builder()
                                .userId(b.getUserId())
                                .nickname(b.getNickname())
                                .imgUrl(b.getImgUrl())
                                .reasons(includeReasons ? List.of("랜덤 보충") : null)
                                .userRank(userRankService.getUserRank(b.getId()))
                                .build())
                        .toList();

                var tmp = new ArrayList<>(list);
                tmp.addAll(extraDtos);
                list = tmp;
            }
        }

        return RecommendUsersResponseDTO.builder().users(list).build();
    }

    @Override
    public MemeResponseDTO getUserMemeCreateHistory(String userId, int page, int size) {
        // 1. 유저
        User user = userRepository.findByUserIdAndDeletedFalse(userId);
        if(user == null){
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        // 2. 페이징
        int safePage = Math.max(1, page);
        int safeSize = Math.max(1, size);
        Pageable pageable = PageRequest.of(safePage - 1, safeSize);

        // 4. 페이지 조회
        Page<Meme> pageResult = memeRepository.findMemesByCreatorIdOrderByCreatedAtDesc(user.getId(), pageable);
        long total = pageResult.getTotalElements();

        // 5. DTO 매핑
        List<MemeItemDTO> items = pageResult.getContent().stream()
                .map(m -> MemeItemDTO.builder()
                        .memeId(m.getId())
                        .memeUrl(m.getImageUrl())
                        .build())
                .toList();

        return MemeResponseDTO.builder()
                .page(safePage)
                .size(safeSize)
                .total(Math.toIntExact(total))
                .items(items)
                .build();
    }

    // >>> CHANGED: 점수 누적 유틸(동일)
    private void add(Map<String, Candidate> map, String userId, int score, String reason) {
        var c = map.computeIfAbsent(userId, k -> new Candidate());
        c.score += score;
        if (reason != null) c.reasons.add(reason);
    }

    // >>> CHANGED: 정렬/DTO 변환 시 NPE 방지용
    private Candidate safeCand(Map<String, Candidate> scores, String uid) {
        Candidate c = scores.get(uid);
        if (c == null) return new Candidate();
        if (c.reasons == null) c.reasons = new ArrayList<>();
        return c;
    }

    private static class Candidate {
        int score = 0;
        List<String> reasons = new ArrayList<>();
    }

    // ====== 아래부터 Redis 키/SCAN 헬퍼들 ======

    // >>> CHANGED: 참여자 Set 키 생성 (한 곳에서 규칙 통일)
    private String roomUsersKey(String roomId) {
        return ROOM_KEY_PREFIX + roomId + ":users";
    }

    // >>> CHANGED: room:* 키를 SCAN하여 artistId가 일치하는 roomId만 반환
    private Set<String> findRoomIdsByArtist(Long artistId) {
        Set<String> roomIds = new HashSet<>();
        var cf = redisTemplate.getConnectionFactory();
        if (cf == null) return roomIds;

        try (var conn = cf.getConnection();
            var cursor = conn.scan(ScanOptions.scanOptions().match(ROOM_KEY_PREFIX + "*").count(1000).build())) {

            while (cursor.hasNext()) {
                String key = new String(cursor.next(), StandardCharsets.UTF_8);
                if (key.contains(":users")) continue;

                // opsForValue()로 값을 가져옵니다.
                Object value = redisTemplate.opsForValue().get(key);

                // 헬퍼를 통해 artistId를 추출합니다.
                Long aid = extractArtistIdFromValue(value);

                if (aid != null && artistId.equals(aid)) {
                    String roomId = key.substring(ROOM_KEY_PREFIX.length());
                    roomIds.add(roomId);
                }
            }
        } catch (Exception ignore) { /* 무시 */ }

        return roomIds;
    }

    /**
     * [최적화] opsForValue()로 가져온 값에서 artistId를 추출하는 헬퍼입니다.
     * 다양한 역직렬화 가능성(DTO, Map, String)을 모두 처리합니다.
     */
    @SuppressWarnings("unchecked")
    private Long extractArtistIdFromValue(Object val) {
        if (val == null) return null;

        try {
            if (val instanceof LiveRoomDTO dto) {
                return dto.getArtistId();
            }
            if (val instanceof Map<?, ?> m) {
                Object v = m.get("artistId");
                if (v instanceof Number n) return n.longValue();
                if (v instanceof String s) return Long.parseLong(s);
            }
            if (val instanceof String s && s.trim().startsWith("{")) {
                Map<String, Object> m = objectMapper.readValue(s, Map.class);
                Object v = m.get("artistId");
                if (v instanceof Number n) return n.longValue();
            }
        } catch (Exception ignore) {
            return null;
        }
        return null;
    }

    public User findByNickname(String nickname) {
        return userRepository.findByNickname(nickname)
                .orElseThrow(() -> new IllegalArgumentException("해당 닉네임의 사용자가 존재하지 않습니다: " + nickname));
    }

    @Override
    public void changePassword(Long userId, PasswordChangeRequestDTO requestDTO) {
        // 1. 입력값 기본 검증
        if(requestDTO.getCurrentPassword() == null || requestDTO.getCurrentPassword().isBlank()){
           throw new CustomException(ErrorCode.CURRENT_PASSWORD_EMPTY);
        }
        if(requestDTO.getNewPassword() == null || requestDTO.getNewPassword().isBlank()){
            throw new CustomException(ErrorCode.NEW_PASSWORD_EMPTY);
        }

        // 2. 새 비밀번호 기본 정책 (백에선 8자리 이상만 체크)
        if(requestDTO.getNewPassword().length() < 8){
            throw new CustomException(ErrorCode.PASSWORD_POLICY_VIOLATION);
        }

        // 3. 사용자 검증
        User user = userRepository.findByIdAndDeletedFalse(userId);
        if(user == null){
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        // 4. 현재 비밀번호 검증
        if(!passwordEncoder.matches(requestDTO.getCurrentPassword(), user.getPassword())){
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        // 5. 기존과 같은 비밀번호 확인
        if (passwordEncoder.matches(requestDTO.getNewPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.SAME_PASSWORD_NOT_ALLOWED);
        }

        // 6. 비밀번호 변경
        user.setPassword(passwordEncoder.encode(requestDTO.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    public void setPassword(Long userId,String newPassword){
        // 1. 입력값 기본 검증
        if(newPassword == null || newPassword.isBlank()){
            throw new CustomException(ErrorCode.NEW_PASSWORD_EMPTY);
        }

        // 2. 새 비밀번호 기본 정책 (백에선 8자리 이상만 체크)
        if(newPassword.length() < 8){
            throw new CustomException(ErrorCode.PASSWORD_POLICY_VIOLATION);
        }

        // 3. 사용자 검증
        User user = userRepository.findByIdAndDeletedFalse(userId);
        if(user == null){
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        // 4. 기존과 같은 비밀번호 확인
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new CustomException(ErrorCode.SAME_PASSWORD_NOT_ALLOWED);
        }

        // 5. 비밀번호 변경
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
