package com.a404.duckonback.common.infra.redis;

import com.a404.duckonback.common.util.GuestNicknameGenerator;
import com.a404.duckonback.domain.artist.artist.entity.Artist;
import com.a404.duckonback.domain.room.dto.*;
import com.a404.duckonback.domain.user.entity.User;
import com.a404.duckonback.common.exception.CustomException;
import com.a404.duckonback.domain.artist.artist.repository.ArtistRepository;
import com.a404.duckonback.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.*;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;

@Service
@RequiredArgsConstructor
public class RedisServiceImpl implements RedisService {

    private final GuestNicknameGenerator guestNicknameGenerator;

    private final @Qualifier("roomTemplate")
    RedisTemplate<String, LiveRoomDTO> roomTemplate;
    // DTO 통저장용
//    private final RedisTemplate<String, LiveRoomDTO> roomTemplate;

    // 집합/문자열 카운터 등 부가기능용
    private final StringRedisTemplate stringRedisTemplate;

    private final UserRepository userRepository;
    private final ArtistRepository artistRepository;

    private static final String ROOM_KEY_PREFIX = "room:";
    private static final String ROOM_USERS_SUFFIX = ":users";
    private static final String ARTIST_ROOMS_PREFIX = "artist:";
    private static final String ARTIST_ROOMS_SUFFIX = ":rooms";
    private static final Duration ROOM_TTL = Duration.ofHours(6);

    private static final String HOST_ROOM_LOCK_PREFIX = "lock:room:create:";

    private static final String GUEST_PROFILE_SUFFIX = ":profile";
    private static final Duration GUEST_TTL = Duration.ofHours(6);

    private String roomKey(String roomId) { return ROOM_KEY_PREFIX + roomId; }
    private String roomKey(Long roomId)   { return ROOM_KEY_PREFIX + roomId; }
    private String roomUsersKey(String roomId) { return ROOM_KEY_PREFIX + roomId + ROOM_USERS_SUFFIX; }
    private String artistRoomsKey(Long artistId) { return ARTIST_ROOMS_PREFIX + artistId + ARTIST_ROOMS_SUFFIX; }

    private String guestProfileKey(String guestId){
        // guestId는 "guest:xxxx"
        // 키는 "guest:xxxx:profile"
        return guestId + GUEST_PROFILE_SUFFIX;
    }

    // ===================== 방 정보 (DTO 통저장) =====================

    public void saveRoomInfo(String roomId, LiveRoomDTO room) {
        roomTemplate.opsForValue().set(roomKey(roomId), room, ROOM_TTL);
    }

    public LiveRoomDTO getRoomInfo(String roomId) {
        return roomTemplate.opsForValue().get(roomKey(roomId));
    }

    public void updateRoomInfo(LiveRoomSyncDTO dto) {
        String key = roomKey(dto.getRoomId());
        LiveRoomDTO cur = roomTemplate.opsForValue().get(key);
        if (cur == null) {
            throw new CustomException("Redis에 저장된 방 정보가 없습니다.", HttpStatus.NOT_FOUND);
        }

        // 필요한 필드만 반영
        cur.setHostId(dto.getHostId());
        cur.setHostNickname(dto.getHostNickname());
        cur.setTitle(dto.getTitle());
        cur.setPlaylist(dto.getPlaylist());
        cur.setCurrentVideoIndex(dto.getCurrentVideoIndex());
        cur.setCurrentTime(dto.getCurrentTime());
        cur.setPlaying(dto.isPlaying());
        cur.setLastUpdated(dto.getLastUpdated());

        // TTL 갱신 포함 저장
        roomTemplate.opsForValue().set(key, cur, ROOM_TTL);
    }

    public void deleteRoomInfo(Long artistId, Long roomId) {
        String rKey = roomKey(roomId);
        String aKey = artistRoomsKey(artistId);
        String uKey = roomUsersKey(roomId.toString());

        Boolean roomDeleted = roomTemplate.delete(rKey);
        Long removed = stringRedisTemplate.opsForSet().remove(aKey, roomId.toString());
        Boolean usersDeleted = stringRedisTemplate.delete(uKey);

        String bKey = "room:" + roomId + ":banned";
        stringRedisTemplate.delete(bKey); // 방 블랙리스트 정리

        if (roomDeleted != null && !roomDeleted) {
            throw new CustomException("roomId에 대한 방이 존재하지 않습니다.", HttpStatus.NOT_FOUND);
        }
        if (removed == null || removed == 0) {
            throw new CustomException("해당 아티스트의 방 목록에 해당 roomId가 존재하지 않습니다.", HttpStatus.NOT_FOUND);
        }
    }

    // ===================== 아티스트-방 매핑 =====================

    public void addRoomToArtist(String artistId, String roomId) {
        String key = ARTIST_ROOMS_PREFIX + artistId + ARTIST_ROOMS_SUFFIX;
        stringRedisTemplate.opsForSet().add(key, roomId);
    }

    // ===================== 참가자 관리 =====================

    @Override
    public Long getRoomUserCount(String roomId) {
        Long fixed = getRoomUserCountStrict(roomId);
        return fixed;
    }


    // ===================== 목록/트렌딩 =====================

    @Override
    public List<LiveRoomSummaryDTO> getAllRoomSummaries(Long artistId) {
        String aKey = artistRoomsKey(artistId);
        Set<String> roomIds = Optional.ofNullable(stringRedisTemplate.opsForSet().members(aKey))
                .orElseGet(Collections::emptySet);

        if (roomIds.isEmpty()) return List.of();

        return roomIds.stream()
                .map(roomId -> {
                    LiveRoomDTO dto = roomTemplate.opsForValue().get(roomKey(roomId));
                    if (dto == null) return null;

                    // 참가자 수
//                    Long userCount = stringRedisTemplate.opsForSet().size(roomUsersKey(roomId));
//                    int participantCount = userCount != null ? userCount.intValue() : 0;
                    int participantCount = Math.toIntExact(getRoomUserCount(roomId));

                    // 호스트 보강
                    User host = userRepository.findByUserIdAndDeletedFalse(dto.getHostId());
                    String hostNickname     = host != null ? host.getNickname() : null;
                    String hostProfileImage = host != null ? host.getImgUrl()    : null;

                    return LiveRoomSummaryDTO.builder()
                            .roomId(dto.getRoomId())
                            .title(dto.getTitle())
                            .hostId(dto.getHostId())
                            .hostNickname(hostNickname)
                            .hostProfileImgUrl(hostProfileImage)
                            .imgUrl(dto.getImgUrl())
                            .participantCount(participantCount)
                            .build();
                })
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public List<RoomListInfoDTO> getTrendingRooms(int size) {
        Page<RoomListInfoDTO> page = getTrendingRooms(PageRequest.of(0, size));
        return page.getContent();
    }

    @Override
    public List<HomeArtistRoomDTO> getHomeArtistRooms(List<Long> artistIds, int roomLimitPerArtist) {
        return artistIds.stream()
            .map(artistId -> {
                Artist artist = artistRepository.findById(artistId).orElse(null);
                if (artist == null) return null;

                // 해당 아티스트의 모든 방 조회
                List<LiveRoomSummaryDTO> rooms = getAllRoomSummaries(artistId);
                // 참여자 수 내림차순 정렬 및 제한한
                List<LiveRoomSummaryDTO> sortedRooms = rooms.stream()
                        .sorted((r1, r2) -> Integer.compare(r2.getParticipantCount(), r1.getParticipantCount()))
                        .limit(roomLimitPerArtist)
                        .toList();

                return HomeArtistRoomDTO.builder()
                        .artistId(artist.getArtistId())
                        .artistName(artist.getNameEn())
                        .artistImgUrl(artist.getImgUrl())
                        .rooms(sortedRooms)
                        .build();
            })
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public Page<RoomListInfoDTO> getTrendingRooms(Pageable pageable) {
        // 1) room:* 스캔
//        Set<String> keys = stringRedisTemplate.keys(ROOM_KEY_PREFIX + "*");
        Set<String> keys = scanKeys(ROOM_KEY_PREFIX + "*");

        if (keys == null || keys.isEmpty()) {
            return Page.empty(pageable);
        }

        // 2) "room:{숫자}"만 추출 (":users", ":info" 등 제거)
        List<String> roomIds = keys.stream()
                .map(k -> k.substring(ROOM_KEY_PREFIX.length()))  // "{id}" 또는 "{id}:users" 등
                .filter(rest -> rest.chars().allMatch(Character::isDigit)) // 숫자만 통과
                .toList();

        if (roomIds.isEmpty()) return Page.empty(pageable);

        // 3) DTO는 항상 "room:{id}" 로 GET
        List<RoomListInfoDTO> all = roomIds.stream()
                .map(roomIdStr -> {
                    String rKey = roomKey(roomIdStr); // "room:{id}"
                    LiveRoomDTO dto = roomTemplate.opsForValue().get(rKey);
                    if (dto == null || dto.getArtistId() == null) return null;

                    // 참여자 수
    //                Long cnt = stringRedisTemplate.opsForSet().size(roomUsersKey(roomIdStr));
    //                int participantCount = cnt != null ? cnt.intValue() : 0;
                    int participantCount = Math.toIntExact(getRoomUserCount(roomIdStr));


                    // host 정보
                    User host = userRepository.findByUserIdAndDeletedFalse(dto.getHostId());
                    String hostNickname   = host != null ? host.getNickname() : null;
                    String hostProfileImg = host != null ? host.getImgUrl()    : null;

                    // artist 정보
                    Artist artist = artistRepository.findById(dto.getArtistId()).orElse(null);
                    String artistNameEn = artist != null ? artist.getNameEn() : null;
                    String artistNameKr = artist != null ? artist.getNameKr() : null;

                    return RoomListInfoDTO.builder()
                            .roomId(dto.getRoomId())
                            .artistId(dto.getArtistId())
                            .artistNameEn(artistNameEn)
                            .artistNameKr(artistNameKr)
                            .title(dto.getTitle())
                            .hostId(dto.getHostId())
                            .hostNickname(hostNickname)
                            .hostProfileImgUrl(hostProfileImg)
                            .imgUrl(dto.getImgUrl())
                            .participantCount(participantCount)
                            .build();
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingInt(RoomListInfoDTO::getParticipantCount).reversed())
                .toList();

        int total = all.size();
        int from = (int) pageable.getOffset();
        if (from >= total) return new PageImpl<>(List.of(), pageable, total);
        int to = Math.min(from + pageable.getPageSize(), total);
        return new PageImpl<>(all.subList(from, to), pageable, total);
    }


    // ===================== 채팅 RateLimit 카운터 =====================

    @Override
    public boolean increaseChatCount(String roomId, String userId) {
        String key = "chat_limit:" + roomId + ":" + userId;
        Long count = stringRedisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            stringRedisTemplate.expire(key, Duration.ofSeconds(5));
        }
        return count != null && count <= 10;
    }


    @Override
    public RoomListInfoDTO getActiveRoomByHost(String hostUserId) {
        // KEYS 대신 SCAN 권장 (간단히 KEYS를 쓰되 "room:{id}"만 통과시키려면 아래 필터는 꼭!)
//        Set<String> keys = stringRedisTemplate.keys(ROOM_KEY_PREFIX + "*");
        Set<String> keys = scanKeys(ROOM_KEY_PREFIX + "*");

        if (keys == null || keys.isEmpty()) return null;

        for (String key : keys) {
            // "room:{id}" 만 허용: "room:{id}:users" 같은 건 스킵
            int nextColon = key.indexOf(':', ROOM_KEY_PREFIX.length());
            if (nextColon != -1) continue;

            LiveRoomDTO dto = roomTemplate.opsForValue().get(key);
            if (dto == null) continue;
            if (!Objects.equals(hostUserId, dto.getHostId())) continue;

            String roomIdStr = key.substring(ROOM_KEY_PREFIX.length());
    //        Long cnt = stringRedisTemplate.opsForSet().size(roomUsersKey(roomIdStr));
    //        int participantCount = cnt != null ? cnt.intValue() : 0;
            int participantCount = Math.toIntExact(getRoomUserCount(roomIdStr));


            User hostUser = userRepository.findByUserIdAndDeletedFalse(hostUserId);
            String hostNickname   = (dto.getHostNickname() != null) ? dto.getHostNickname()
                    : (hostUser != null ? hostUser.getNickname() : null);
            String hostProfileImg = hostUser != null ? hostUser.getImgUrl() : null;

            Artist artist = (dto.getArtistId() != null) ? artistRepository.findById(dto.getArtistId()).orElse(null) : null;

            return RoomListInfoDTO.builder()
                    .roomId(dto.getRoomId())
                    .artistId(dto.getArtistId())
                    .artistNameEn(artist != null ? artist.getNameEn() : null)
                    .artistNameKr(artist != null ? artist.getNameKr() : null)
                    .title(dto.getTitle())
                    .hostId(hostUserId)
                    .hostNickname(hostNickname)
                    .hostProfileImgUrl(hostProfileImg)
                    .imgUrl(dto.getImgUrl())
                    .participantCount(participantCount)
                    .build();
        }
        return null;
    }

    @Override
    public boolean isUserBanned(String roomId, String userId) {
        String bKey = "room:" + roomId + ":banned";
        return Boolean.TRUE.equals(
                stringRedisTemplate.opsForSet().isMember(bKey, userId)
        );
    }

    @Override
    public boolean acquireCreateRoomLock(String hostUserId) {
        String key = HOST_ROOM_LOCK_PREFIX + hostUserId;
        // 이미 키가 있으면 false, 없으면 생성하고 true
        Boolean ok = stringRedisTemplate.opsForValue()
                .setIfAbsent(key, "1", Duration.ofSeconds(5)); // 5초 정도 TTL (취향 따라 조절)
        return Boolean.TRUE.equals(ok);
    }

    @Override
    public void releaseCreateRoomLock(String hostUserId) {
        String key = HOST_ROOM_LOCK_PREFIX + hostUserId;
        stringRedisTemplate.delete(key);
    }

    @Override
    public Set<String> scanKeys(String pattern) {
        Set<String> keys = new HashSet<>();

        ScanOptions options = ScanOptions.scanOptions()
                .match(pattern)
                .count(1000)  // 한 번에 스캔할 양 힌트
                .build();

        stringRedisTemplate.execute((RedisConnection connection) -> {
            try (Cursor<byte[]> cursor = connection.scan(options)) {
                while (cursor.hasNext()) {
                    byte[] rawKey = cursor.next();
                    keys.add(new String(rawKey, StandardCharsets.UTF_8));
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to scan keys", e);
            }
            return null;
        });

        return keys;
    }

    @Override
    public String getOrCreateGuestNickname(String guestId) {
        String key = guestProfileKey(guestId);

        Object existing = stringRedisTemplate.opsForHash().get(key, "nickname");
        if(existing != null){
            stringRedisTemplate.expire(key, GUEST_TTL);
            return existing.toString();
        }

        String nickname = guestNicknameGenerator.generateNickname();

        // 최초 1회만 저장
        Boolean ok = stringRedisTemplate.opsForHash().putIfAbsent(key, "nickname", nickname);
        stringRedisTemplate.expire(key, GUEST_TTL);

        // putIfAbsent가 false여도(레이스) 이미 누가 넣었을 테이 다시 읽어옴
        Object saved = stringRedisTemplate.opsForHash().get(key, "nickname");
        return saved != null ? saved.toString() : nickname;
    }

    @Override
    public boolean addViewerToRoom(String roomId, String viewerId) {
        String uKey = roomUsersKey(roomId);

        // 방 존재 여부 확인, DTO가 없으면 방이 없는 것으로 간주, false 반환
        Boolean roomExists = roomTemplate.hasKey(roomKey(roomId));
        if(!Boolean.TRUE.equals(roomExists)){
            return false;
        }

        Long added = stringRedisTemplate.opsForSet().add(uKey, viewerId);

        // user set에도 TTL 걸어두기
        stringRedisTemplate.expire(uKey, ROOM_TTL);

        return added != null && added > 0;

    }

    @Override
    public boolean removeViewerFromRoom(String artistId, String roomId, String viewerId) {
        String uKey = roomUsersKey(roomId);
        String rKey = roomKey(roomId);
        String aKey = ARTIST_ROOMS_PREFIX + artistId + ARTIST_ROOMS_SUFFIX;

        // 멱등 처리: 방이 이미 삭제됐으면 false 반환
        Boolean roomExists = roomTemplate.hasKey(rKey);
        if(!Boolean.TRUE.equals(roomExists)) {
            return false;
        }

        Long removed = stringRedisTemplate.opsForSet().remove(uKey, viewerId);

        // 마지막 유저라면 방 정리
        Long size = stringRedisTemplate.opsForSet().size(uKey);
        if(size != null && size == 0L) {
            stringRedisTemplate.delete(uKey);
            roomTemplate.delete(rKey);
            stringRedisTemplate.opsForSet().remove(aKey, roomId);

            // 방 블랙리스트 정리
            String bKey = "room:" + roomId + ":banned";
            stringRedisTemplate.delete(bKey);

        }
        return removed != null && removed > 0;
    }

    @Override
    public Long getRoomUserCountStrict(String roomId) {
        String uKey = roomUsersKey(roomId);
        Long size = stringRedisTemplate.opsForSet().size(uKey);
        return size == null ? 0L : size;
    }
}
