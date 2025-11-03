//package com.a404.duckonback.service;
//
//import com.a404.duckonback.dto.LiveRoomDTO;
//import com.a404.duckonback.dto.LiveRoomSummaryDTO;
//import com.a404.duckonback.dto.LiveRoomSyncDTO;
//import com.a404.duckonback.dto.RoomListInfoDTO;
//import com.a404.duckonback.entity.Artist;
//import com.a404.duckonback.entity.User;
//import com.a404.duckonback.exception.CustomException;
//import com.a404.duckonback.repository.ArtistRepository;
//import com.a404.duckonback.repository.UserRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.data.domain.*;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.http.HttpStatus;
//import org.springframework.stereotype.Service;
//
//import java.time.Duration;
//import java.util.*;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//public class RedisServiceImpl implements RedisService {
//    private final RedisTemplate<String, Object> redisTemplate;
//    private final UserRepository userRepository;
//    private final ArtistRepository artistRepository;
//
//    public void saveRoomInfo(String roomId, LiveRoomDTO room) {
//        String key = "room:" + roomId + ":info";
//
//        Map<String, Object> map = new HashMap<>();
//        map.put("title", room.getTitle());
//        map.put("artistId", room.getArtistId());
//        map.put("hostId", room.getHostId());
//        map.put("hostNickname", room.getHostNickname());
//        map.put("imgUrl", room.getImgUrl());
//        map.put("playlist", room.getPlaylist());
//        map.put("currentVideoIndex", room.getCurrentVideoIndex());
//        map.put("currentTime", room.getCurrentTime());
//        map.put("playing", room.isPlaying());
//        map.put("lastUpdated", room.getLastUpdated());
//        map.put("locked", room.isLocked());
//        map.put("entryQuestion", room.getEntryQuestion());
//        map.put("entryAnswer", room.getEntryAnswer());
//
//
//        redisTemplate.opsForHash().putAll(key, map);
//        redisTemplate.expire(key, Duration.ofHours(6));
//    }
//
//    public void updateRoomInfo(LiveRoomSyncDTO dto){
//        LiveRoomDTO room = getRoomInfo(dto.getRoomId().toString());
//        if (room == null) {
//            throw new CustomException("Redis에 저장된 방 정보가 없습니다.", HttpStatus.NOT_FOUND);
//        }
//
//        room.setHostId(dto.getHostId());
//        room.setHostNickname(dto.getHostNickname());
//        room.setTitle(dto.getTitle());
//        room.setPlaylist(dto.getPlaylist());
//        room.setCurrentVideoIndex(dto.getCurrentVideoIndex());
//        room.setCurrentTime(dto.getCurrentTime());
//        room.setPlaying(dto.isPlaying());
//        room.setLastUpdated(dto.getLastUpdated());
//
//        saveRoomInfo(dto.getRoomId().toString(), room);
//    }
//
//    public void addRoomToArtist(String artistId, String roomId) {
//        String key = "artist:" + artistId + ":rooms";
//        redisTemplate.opsForSet().add(key, roomId);
//    }
//
//    public LiveRoomDTO getRoomInfo(String roomId) {
//        String key = "room:" + roomId + ":info";
//        Map<Object, Object> map = redisTemplate.opsForHash().entries(key);
//
//        if (map.isEmpty()) {
//            throw new CustomException("Redis에 해당 roomId의 방 정보가 존재하지 않습니다.", HttpStatus.NOT_FOUND);
//        }
//
//        // artistId 꺼내서 Long으로 변환
//        Object artistIdObj = map.get("artistId");
//        Long artistId = null;
//        if (artistIdObj != null) {
//            // Redis에 저장된 값이 "\"123\"" 같은 형태일 수 있으니, toString() 후 숫자만 파싱
//            String artistIdStr = artistIdObj.toString().replace("\"", "");
//            artistId = Long.valueOf(artistIdStr);
//        }
//
//        return LiveRoomDTO.builder()
//                .roomId(Long.valueOf((roomId)))
//                .title((String) map.get("title"))
//                .artistId(artistId)
//                .hostId((String) map.get("hostId"))
//                .hostNickname((String) map.get("hostNickname"))
//                .imgUrl((String) map.get("imgUrl"))
//                .playlist((List<String>) map.get("playlist"))
//                .currentVideoIndex((int) map.get("currentVideoIndex"))
//                .currentTime((double) map.get("currentTime"))
//                .playing((boolean) map.get("playing"))
//                .lastUpdated((long) map.get("lastUpdated"))
//                .locked(Boolean.parseBoolean(map.getOrDefault("locked", "false").toString()))
//                .entryQuestion((String) map.getOrDefault("entryQuestion", null))
//                .entryAnswer((String) map.getOrDefault("entryAnswer", null))
//                .build();
//    }
//
//
//
//    public void deleteRoomInfo(Long artistId, Long roomId) {
//        String roomKey = "room:" + roomId + ":info";
//        String artistRoomsKey = "artist:" + artistId + ":rooms";
//        String roomUsersKey = "room:" + roomId + ":users";
//
//        Boolean deleted = redisTemplate.delete(roomKey);
//        Long removed = redisTemplate.opsForSet().remove(artistRoomsKey, roomId.toString());
//        Boolean deletedUsers = redisTemplate.delete(roomUsersKey);
//
//        if (deleted != null && deleted == false) {
//            throw new CustomException("roomId에 대한 방이 존재하지 않습니다.", HttpStatus.NOT_FOUND);
//        }
//
//        if (removed == 0) {
//            throw new CustomException("해당 아티스트의 방 목록에 해당 roomId가 존재하지 않습니다.", HttpStatus.NOT_FOUND);
//        }
//    }
//
//    @Override
//    public void addUserToRoom(String roomId, User user) {
//        String key = "room:" + roomId + ":users";
//        String userInfo = user.getUserId();
//
//        redisTemplate.opsForSet().add(key, userInfo);
//        redisTemplate.expire(key, Duration.ofHours(6));
//    }
//
//    @Override
//    public void removeUserFromRoom(String artistId, String roomId, User user){
//        String userSetKey = "room:" + roomId + ":users";
//        String roomInfoKey = "room:" + roomId + ":info";
//        String artistRoomsKey = "artist:" + artistId + ":rooms";
//
//        String userInfo = user.getUserId();
//
//        Set<String> userSetBeforeRemoval = redisTemplate.opsForSet()
//                .members(userSetKey)
//                .stream().map(Object::toString).collect(Collectors.toSet());
//
//        Map<Object, Object> backupRoomInfo = redisTemplate.opsForHash().entries(roomInfoKey);
//        boolean wasInArtistRooms = redisTemplate.opsForSet().isMember(artistRoomsKey, roomId);
//
//        // 유저 제거
//        redisTemplate.opsForSet().remove(userSetKey, userInfo);
//
//        Long size = redisTemplate.opsForSet().size(userSetKey);
//        if (size != null && size == 0L) {
//
//            Boolean deleted = redisTemplate.delete(userSetKey);
//
//            if (deleted == null) {
//                throw new CustomException("접속 유저 목록 삭제 중 오류", HttpStatus.INTERNAL_SERVER_ERROR);
//            }
//
//            boolean deletedRoomInfo = redisTemplate.delete(roomInfoKey);
//            if (!deletedRoomInfo) {
//                // 롤백
//                if (!userSetBeforeRemoval.isEmpty()) {
//                    redisTemplate.opsForSet().add(userSetKey, userSetBeforeRemoval.toArray());
//                }
//                throw new CustomException("방 정보 삭제 실패", HttpStatus.INTERNAL_SERVER_ERROR);
//            }
//
//            Long removed = redisTemplate.opsForSet().remove(artistRoomsKey, roomId);
//            if (removed == null || removed == 0) {
//                // 롤백
//                if (!userSetBeforeRemoval.isEmpty()) {
//                    redisTemplate.opsForSet().add(userSetKey, userSetBeforeRemoval.toArray());
//                }
//                if (!backupRoomInfo.isEmpty()) {
//                    redisTemplate.opsForHash().putAll(roomInfoKey, backupRoomInfo);
//                }
//                if (wasInArtistRooms) {
//                    redisTemplate.opsForSet().add(artistRoomsKey, roomId);
//                }
//                throw new CustomException("아티스트 방 목록에서 제거 실패", HttpStatus.INTERNAL_SERVER_ERROR);
//            }
//        }else if (size != null && size > 0L) {
//            //host 변경 로직 : 프론트에서 새로 방 정보 전송하는걸로 변경
////            String newHostId = redisTemplate.opsForSet().members(userSetKey)
////                    .stream()
////                    .map(Object::toString)
////                    .findFirst()
////                    .orElse(null);
////
////            if (newHostId != null) {
////                redisTemplate.opsForHash().put(roomInfoKey, "hostId", newHostId);
////            }
//
//        }
//    }
//
//    @Override
//    public List<LiveRoomSummaryDTO> getAllRoomSummaries(Long artistId) {
//        String artistRoomsKey = "artist:" + artistId + ":rooms";
//        Set<Object> roomIds = redisTemplate.opsForSet().members(artistRoomsKey);
//        if (roomIds == null || roomIds.isEmpty()) {
//            return List.of();
//        }
//
//        return roomIds.stream()
//                .map(Object::toString)
//                .map(roomId -> {
//                    String roomInfoKey = "room:" + roomId + ":info";
//                    Map<Object, Object> roomMap = redisTemplate.opsForHash().entries(roomInfoKey);
//                    if (roomMap.isEmpty()) return null;
//
//                    // 참가자 수 조회
//                    Long userCount = redisTemplate.opsForSet().size("room:" + roomId + ":users");
//                    int participantCount = userCount != null ? userCount.intValue() : 0;
//
//                    String hostUserId = (String) roomMap.get("hostId");
//                    String title      = (String) roomMap.get("title");
//                    String imgUrl     = (String) roomMap.get("imgUrl");
//
//                    // hostId로 User 조회 (null 체크)
//                    User host = userRepository.findByUserIdAndDeletedFalse(hostUserId);
//                    String hostNickname     = host != null ? host.getNickname() : null;
//                    String hostProfileImage = host != null ? host.getImgUrl()    : null;
//
//                    return LiveRoomSummaryDTO.builder()
//                            .roomId(Long.valueOf(roomId))
//                            .title(title)
//                            .hostId(hostUserId)
//                            .hostNickname(hostNickname)
//                            .hostProfileImgUrl(hostProfileImage)
//                            .imgUrl(imgUrl)
//                            .participantCount(participantCount)
//                            .build();
//                })
//                .filter(Objects::nonNull)
//                .toList();
//    }
//
//    @Override
//    public List<RoomListInfoDTO> getTrendingRooms(int size) {
//        Page<RoomListInfoDTO> page = getTrendingRooms(PageRequest.of(0, size));
//        return page.getContent();
//    }
//
//    /** 전체 방 중 참여자 수 기준 Top-N을 TrendingRoomDTO로 반환 */
//    @Override
//    public Page<RoomListInfoDTO> getTrendingRooms(Pageable pageable) {
//        Set<String> keys = redisTemplate.keys("room:*:info");
//        if (keys == null || keys.isEmpty()) {
//            return Page.empty(pageable);
//        }
//
//        List<RoomListInfoDTO> all = keys.stream()
//                .map(key -> {
//                    // key = room:{roomId}:info
//                    String[] parts = key.split(":");
//                    if (parts.length < 3) return null;
//                    String roomId = parts[1];
//
//                    Map<Object, Object> m = redisTemplate.opsForHash().entries(key);
//                    if (m == null || m.isEmpty()) return null;
//
//                    // 참여자 수
//                    Long cnt = redisTemplate.opsForSet().size("room:" + roomId + ":users");
//                    int participantCount = cnt != null ? cnt.intValue() : 0;
//
//                    // artistId 필수
//                    Object artistIdObj = m.get("artistId");
//                    if (artistIdObj == null) return null;
//                    Long artistId;
//                    try {
//                        artistId = Long.valueOf(artistIdObj.toString());
//                    } catch (NumberFormatException e) {
//                        return null;
//                    }
//
//                    // 기본 정보
//                    String title   = (String) m.get("title");
//                    String imgUrl  = (String) m.get("imgUrl");
//                    String hostUid = (String) m.get("hostId");
//
//                    // host 정보
//                    User host = userRepository.findByUserIdAndDeletedFalse(hostUid);
//                    String hostNickname   = host != null ? host.getNickname() : null;
//                    String hostProfileImg = host != null ? host.getImgUrl()    : null;
//
//                    // artist 정보
//                    Artist artist = artistRepository.findById(artistId).orElse(null);
//                    String artistNameEn = artist != null ? artist.getNameEn() : null;
//                    String artistNameKr = artist != null ? artist.getNameKr() : null;
//
//                    return RoomListInfoDTO.builder()
//                            .roomId(Long.valueOf(roomId))
//                            .artistId(artistId)
//                            .artistNameEn(artistNameEn)
//                            .artistNameKr(artistNameKr)
//                            .title(title)
//                            .hostId(hostUid)
//                            .hostNickname(hostNickname)
//                            .hostProfileImgUrl(hostProfileImg)
//                            .imgUrl(imgUrl)
//                            .participantCount(participantCount)
//                            .build();
//                })
//                .filter(Objects::nonNull)
//                .sorted(Comparator.comparingInt(RoomListInfoDTO::getParticipantCount).reversed())
//                .toList();
//
//        int total = all.size();
//        int from = (int) pageable.getOffset();
//        if (from >= total) return new PageImpl<>(List.of(), pageable, total);
//        int to = Math.min(from + pageable.getPageSize(), total);
//
//        return new PageImpl<>(all.subList(from, to), pageable, total);
//    }
//
//
//    @Override
//    public Long getRoomUserCount(String roomId) {
//        String key = "room:" + roomId + ":users";
//        Long size = redisTemplate.opsForSet().size(key);
//        return size == null ? 0L : size;
//    }
//
//    @Override
//    public boolean increaseChatCount(String roomId, String userId) {
//        String key = "chat_limit:" + roomId + ":" + userId;
//        Long count = redisTemplate.opsForValue().increment(key);
//
//        if (count != null && count == 1) {
//            redisTemplate.expire(key, Duration.ofSeconds(5));
//        }
//
//        return count != null && count <= 10;
//    }
//
//    /**
//     * 특정 호스트가 현재 활성화된 방을 조회합니다.
//     * @param hostUserId 호스트의 사용자 ID
//     * @return 활성화된 방 정보, 없으면 null
//     */
//    // 키 스캔 방식 — 방 수가 많아져도 수백~수천이면 충분
//    @Override
//    public RoomListInfoDTO getActiveRoomByHost(String hostUserId) {
//        Set<String> keys = redisTemplate.keys("room:*:info");
//        if (keys == null || keys.isEmpty()) return null;
//
//        for (String key : keys) {
//            Map<Object, Object> m = redisTemplate.opsForHash().entries(key);
//            if (m == null || m.isEmpty()) continue;
//
//            String host = (String) m.get("hostId");
//            if (!Objects.equals(hostUserId, host)) continue;
//
//            String[] parts = key.split(":"); // room:{roomId}:info
//            if (parts.length < 3) continue;
//            String roomIdStr = parts[1];
//
//            Long cnt = redisTemplate.opsForSet().size("room:" + roomIdStr + ":users");
//            int participantCount = cnt != null ? cnt.intValue() : 0;
//
//            Long artistId = null;
//            Object artistIdObj = m.get("artistId");
//            if (artistIdObj != null) {
//                try { artistId = Long.valueOf(artistIdObj.toString()); } catch (NumberFormatException ignore) {}
//            }
//
//            // host/artist 보강 (있으면)
//            User hostUser = userRepository.findByUserIdAndDeletedFalse(hostUserId);
//            String hostNickname   = hostUser != null ? hostUser.getNickname() : null;
//            String hostProfileImg = hostUser != null ? hostUser.getImgUrl()    : null;
//
//            Artist artist = (artistId != null) ? artistRepository.findById(artistId).orElse(null) : null;
//            String artistNameEn = artist != null ? artist.getNameEn() : null;
//            String artistNameKr = artist != null ? artist.getNameKr() : null;
//
//            return RoomListInfoDTO.builder()
//                    .roomId(Long.valueOf(roomIdStr))
//                    .artistId(artistId)
//                    .artistNameEn(artistNameEn)
//                    .artistNameKr(artistNameKr)
//                    .title((String) m.get("title"))
//                    .hostId(hostUserId)
//                    .hostNickname(hostNickname)
//                    .hostProfileImgUrl(hostProfileImg)
//                    .imgUrl((String) m.get("imgUrl"))
//                    .participantCount(participantCount)
//                    .build();
//        }
//        return null;
//    }
//
//
//
//}

package com.a404.duckonback.service;

import com.a404.duckonback.dto.LiveRoomDTO;
import com.a404.duckonback.dto.LiveRoomSummaryDTO;
import com.a404.duckonback.dto.LiveRoomSyncDTO;
import com.a404.duckonback.dto.RoomListInfoDTO;
import com.a404.duckonback.dto.HomeArtistRoomDTO;
import com.a404.duckonback.entity.Artist;
import com.a404.duckonback.entity.User;
import com.a404.duckonback.exception.CustomException;
import com.a404.duckonback.repository.ArtistRepository;
import com.a404.duckonback.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RedisServiceImpl implements RedisService {

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
    private static final String ROOM_COUNT_SUFFIX = ":count";

    private String roomCountKey(String roomId) { return ROOM_KEY_PREFIX + roomId + ROOM_COUNT_SUFFIX; }
    private String roomCountKey(Long roomId)   { return ROOM_KEY_PREFIX + roomId + ROOM_COUNT_SUFFIX; }
    private String roomKey(String roomId) { return ROOM_KEY_PREFIX + roomId; }
    private String roomKey(Long roomId)   { return ROOM_KEY_PREFIX + roomId; }
    private String roomUsersKey(String roomId) { return ROOM_KEY_PREFIX + roomId + ROOM_USERS_SUFFIX; }
    private String artistRoomsKey(Long artistId) { return ARTIST_ROOMS_PREFIX + artistId + ARTIST_ROOMS_SUFFIX; }

    // ===================== 방 정보 (DTO 통저장) =====================

    public void saveRoomInfo(String roomId, LiveRoomDTO room) {
        roomTemplate.opsForValue().set(roomKey(roomId), room, ROOM_TTL);

        // 참여자 수 카운터를 0으로 초기화 (이미 있으면 건들지 않음)
        String cKey = roomCountKey(roomId);
        Boolean created = stringRedisTemplate.opsForValue().setIfAbsent(cKey, "0");
        stringRedisTemplate.expire(cKey, ROOM_TTL);
    }

    public LiveRoomDTO getRoomInfo(String roomId) {
        LiveRoomDTO dto = roomTemplate.opsForValue().get(roomKey(roomId));
        if (dto == null) {
            throw new CustomException("Redis에 해당 roomId의 방 정보가 존재하지 않습니다.", HttpStatus.NOT_FOUND);
        }
        return dto;
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
        String cKey = roomCountKey(roomId);

        Boolean roomDeleted = roomTemplate.delete(rKey);
        Long removed = stringRedisTemplate.opsForSet().remove(aKey, roomId.toString());
        Boolean usersDeleted = stringRedisTemplate.delete(uKey);
        stringRedisTemplate.delete(cKey); // 카운터 정리

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
    public void addUserToRoom(String roomId, User user) {
        String uKey = roomUsersKey(roomId);
        String cKey = roomCountKey(roomId);

        Long added = stringRedisTemplate.opsForSet().add(uKey, user.getUserId());

        // 실제로 추가되었을 때만 카운터 +1
        if (added != null && added > 0) {
            addParticipantCountToRoom(roomId);
        }
    }

    @Override
    public void addParticipantCountToRoom(String roomId){
        String cKey = roomCountKey(roomId);
        stringRedisTemplate.opsForValue().increment(cKey);
    }

    @Override
    public void decreaseParticipantCountFromRoom(String roomId){
        String cKey = roomCountKey(roomId);
        stringRedisTemplate.opsForValue().decrement(cKey);
    }


    //    @Override
//    public void removeUserFromRoom(String artistId, String roomId, User user) {
//        String uKey = roomUsersKey(roomId);
//        String rKey = roomKey(roomId);
//        String aKey = ARTIST_ROOMS_PREFIX + artistId + ARTIST_ROOMS_SUFFIX;
//
//        // 현재 유저셋 백업(롤백 대비)
//        Set<String> before = Optional.ofNullable(stringRedisTemplate.opsForSet().members(uKey))
//                .orElseGet(Collections::emptySet);
//
//        // 현재 DTO 백업(롤백 대비)
//        LiveRoomDTO backup = roomTemplate.opsForValue().get(rKey);
//
//        // 제거
//        stringRedisTemplate.opsForSet().remove(uKey, user.getUserId());
//
//        Long size = stringRedisTemplate.opsForSet().size(uKey);
//        if (size != null && size == 0L) {
//            // 마지막 사용자면 방 정리
//            Boolean deletedUsers = stringRedisTemplate.delete(uKey);
//            Boolean deletedRoom  = roomTemplate.delete(rKey);
//            Long removedFromArtist = stringRedisTemplate.opsForSet().remove(aKey, roomId);
//
//            // 실패 시 간단 롤백
//            if ((deletedRoom != null && !deletedRoom) || removedFromArtist == null || removedFromArtist == 0) {
//                // 롤백
//                if (backup != null) {
//                    roomTemplate.opsForValue().set(rKey, backup, ROOM_TTL);
//                }
//                if (!before.isEmpty()) {
//                    stringRedisTemplate.opsForSet().add(uKey, before.toArray(String[]::new));
//                }
//                throw new CustomException("방 정보 삭제 실패", HttpStatus.INTERNAL_SERVER_ERROR);
//            }
//        }
//        // 남아있는 경우의 host 변경은 프론트에서 재전송하는 정책(주석 그대로 유지)
//    }
    @Override
    public void removeUserFromRoom(String artistId, String roomId, User user) {
        String uKey = roomUsersKey(roomId);
        String rKey = roomKey(roomId);
        String aKey = ARTIST_ROOMS_PREFIX + artistId + ARTIST_ROOMS_SUFFIX;
        String cKey = roomCountKey(roomId);

        // 롤백 대비 백업
        Set<String> before = Optional.ofNullable(stringRedisTemplate.opsForSet().members(uKey))
                .orElseGet(Collections::emptySet);
        LiveRoomDTO backup = roomTemplate.opsForValue().get(rKey);

        // 제거 시도
        Long removedUser = stringRedisTemplate.opsForSet().remove(uKey, user.getUserId());
        if (removedUser != null && removedUser > 0) {
            // 실제로 빠졌으면 카운터 -1 (음수 방지)
            Long after = stringRedisTemplate.opsForValue().decrement(cKey);
            if (after != null && after < 0) {
                stringRedisTemplate.opsForValue().set(cKey, "0");
            }
        }

        Long size = stringRedisTemplate.opsForSet().size(uKey);
        if (size != null && size == 0L) {
            // 마지막 사용자면 방 정리
            Boolean deletedUsers = stringRedisTemplate.delete(uKey);
            Boolean deletedRoom  = roomTemplate.delete(rKey);
            Long removedFromArtist = stringRedisTemplate.opsForSet().remove(aKey, roomId);
            // 카운터 키도 정리
            stringRedisTemplate.delete(cKey);

            if ((deletedRoom != null && !deletedRoom) || removedFromArtist == null || removedFromArtist == 0) {
                // 롤백
                if (backup != null) roomTemplate.opsForValue().set(rKey, backup, ROOM_TTL);
                if (!before.isEmpty()) stringRedisTemplate.opsForSet().add(uKey, before.toArray(String[]::new));
                // 카운터를 집합 크기로 복원
                Long fixed = stringRedisTemplate.opsForSet().size(uKey);
                stringRedisTemplate.opsForValue().set(cKey, String.valueOf(fixed == null ? 0 : fixed));
                stringRedisTemplate.expire(uKey, ROOM_TTL);
                stringRedisTemplate.expire(cKey, ROOM_TTL);

                throw new CustomException("방 정보 삭제 실패", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
    }

    @Override
    public Long getRoomUserCount(String roomId) {
        String cKey = roomCountKey(roomId);
        String val = stringRedisTemplate.opsForValue().get(cKey);
        if (val != null) {
            try { return Long.valueOf(val); } catch (NumberFormatException ignore) {}
        }
        // 카운터가 없으면 집합 크기로 보정하고 카운터 저장
        Long size = stringRedisTemplate.opsForSet().size(roomUsersKey(roomId));
        long fixed = (size == null ? 0L : size);
        stringRedisTemplate.opsForValue().set(cKey, String.valueOf(fixed));
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

//    @Override
//    public Page<RoomListInfoDTO> getTrendingRooms(Pageable pageable) {
//        // room:* 값 스캔 (주의: 운영 대규모면 SCAN 커서 방식 권장)
//        Set<String> keys = stringRedisTemplate.keys(ROOM_KEY_PREFIX + "*");
//        if (keys == null || keys.isEmpty()) {
//            return Page.empty(pageable);
//        }
//
//        List<RoomListInfoDTO> all = keys.stream()
//                .map(key -> {
//                    // key = room:{roomId}
//                    String[] parts = key.split(":");
//                    if (parts.length < 2) return null;
//                    String roomIdStr = parts[1];
//
//                    LiveRoomDTO dto = roomTemplate.opsForValue().get(key);
//                    if (dto == null || dto.getArtistId() == null) return null;
//
//                    // 참여자 수
//                    Long cnt = stringRedisTemplate.opsForSet().size(roomUsersKey(roomIdStr));
//                    int participantCount = cnt != null ? cnt.intValue() : 0;
//
//                    // host 정보
//                    User host = userRepository.findByUserIdAndDeletedFalse(dto.getHostId());
//                    String hostNickname   = host != null ? host.getNickname() : null;
//                    String hostProfileImg = host != null ? host.getImgUrl()    : null;
//
//                    // artist 정보
//                    Artist artist = artistRepository.findById(dto.getArtistId()).orElse(null);
//                    String artistNameEn = artist != null ? artist.getNameEn() : null;
//                    String artistNameKr = artist != null ? artist.getNameKr() : null;
//
//                    return RoomListInfoDTO.builder()
//                            .roomId(dto.getRoomId())
//                            .artistId(dto.getArtistId())
//                            .artistNameEn(artistNameEn)
//                            .artistNameKr(artistNameKr)
//                            .title(dto.getTitle())
//                            .hostId(dto.getHostId())
//                            .hostNickname(hostNickname)
//                            .hostProfileImgUrl(hostProfileImg)
//                            .imgUrl(dto.getImgUrl())
//                            .participantCount(participantCount)
//                            .build();
//                })
//                .filter(Objects::nonNull)
//                .sorted(Comparator.comparingInt(RoomListInfoDTO::getParticipantCount).reversed())
//                .toList();
//
//        int total = all.size();
//        int from = (int) pageable.getOffset();
//        if (from >= total) return new PageImpl<>(List.of(), pageable, total);
//        int to = Math.min(from + pageable.getPageSize(), total);
//        return new PageImpl<>(all.subList(from, to), pageable, total);
//    }
@Override
public Page<RoomListInfoDTO> getTrendingRooms(Pageable pageable) {
    // 1) room:* 스캔
    Set<String> keys = stringRedisTemplate.keys(ROOM_KEY_PREFIX + "*");
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

    // ===================== 호스트 활성 방 조회 =====================

//    @Override
//    public RoomListInfoDTO getActiveRoomByHost(String hostUserId) {
//        Set<String> keys = stringRedisTemplate.keys(ROOM_KEY_PREFIX + "*");
//        if (keys == null || keys.isEmpty()) return null;
//
//        for (String key : keys) {
//            LiveRoomDTO dto = roomTemplate.opsForValue().get(key);
//            if (dto == null) continue;
//            if (!Objects.equals(hostUserId, dto.getHostId())) continue;
//
//            String roomIdStr = key.substring(ROOM_KEY_PREFIX.length());
//            Long cnt = stringRedisTemplate.opsForSet().size(roomUsersKey(roomIdStr));
//            int participantCount = cnt != null ? cnt.intValue() : 0;
//
//            User hostUser = userRepository.findByUserIdAndDeletedFalse(hostUserId);
//            String hostNickname   = hostUser != null ? hostUser.getNickname() : null;
//            String hostProfileImg = hostUser != null ? hostUser.getImgUrl()    : null;
//
//            Artist artist = (dto.getArtistId() != null) ? artistRepository.findById(dto.getArtistId()).orElse(null) : null;
//            String artistNameEn = artist != null ? artist.getNameEn() : null;
//            String artistNameKr = artist != null ? artist.getNameKr() : null;
//
//            return RoomListInfoDTO.builder()
//                    .roomId(dto.getRoomId())
//                    .artistId(dto.getArtistId())
//                    .artistNameEn(artistNameEn)
//                    .artistNameKr(artistNameKr)
//                    .title(dto.getTitle())
//                    .hostId(hostUserId)
//                    .hostNickname(hostNickname)
//                    .hostProfileImgUrl(hostProfileImg)
//                    .imgUrl(dto.getImgUrl())
//                    .participantCount(participantCount)
//                    .build();
//        }
//        return null;
//    }
@Override
public RoomListInfoDTO getActiveRoomByHost(String hostUserId) {
    // KEYS 대신 SCAN 권장 (간단히 KEYS를 쓰되 "room:{id}"만 통과시키려면 아래 필터는 꼭!)
    Set<String> keys = stringRedisTemplate.keys(ROOM_KEY_PREFIX + "*");
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

}
