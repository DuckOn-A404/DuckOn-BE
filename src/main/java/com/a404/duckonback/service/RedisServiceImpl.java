package com.a404.duckonback.service;

import com.a404.duckonback.dto.LiveRoomDTO;
import com.a404.duckonback.dto.LiveRoomSummaryDTO;
import com.a404.duckonback.dto.LiveRoomSyncDTO;
import com.a404.duckonback.dto.TrendingRoomDTO;
import com.a404.duckonback.entity.Artist;
import com.a404.duckonback.entity.User;
import com.a404.duckonback.exception.CustomException;
import com.a404.duckonback.repository.ArtistRepository;
import com.a404.duckonback.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RedisServiceImpl implements RedisService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final UserRepository userRepository;
    private final ArtistRepository artistRepository;

    public void saveRoomInfo(String roomId, LiveRoomDTO room) {
        String key = "room:" + roomId + ":info";

        Map<String, Object> map = new HashMap<>();
        map.put("title", room.getTitle());
        map.put("artistId", room.getArtistId());
        map.put("hostId", room.getHostId());
        map.put("imgUrl", room.getImgUrl());
        map.put("playlist", room.getPlaylist());
        map.put("currentVideoIndex", room.getCurrentVideoIndex());
        map.put("currentTime", room.getCurrentTime());
        map.put("playing", room.isPlaying());
        map.put("lastUpdated", room.getLastUpdated());
        map.put("isLocked", room.isLocked());
        map.put("entryQuestion", room.getEntryQuestion());
        map.put("entryAnswer", room.getEntryAnswer());


        redisTemplate.opsForHash().putAll(key, map);
        redisTemplate.expire(key, Duration.ofHours(6));
    }

    public void updateRoomInfo(LiveRoomSyncDTO dto){
        LiveRoomDTO room = getRoomInfo(dto.getRoomId().toString());
        if (room == null) {
            throw new CustomException("Redis에 저장된 방 정보가 없습니다.", HttpStatus.NOT_FOUND);
        }

        room.setHostId(dto.getHostId());
        room.setPlaylist(dto.getPlaylist());
        room.setCurrentVideoIndex(dto.getCurrentVideoIndex());
        room.setCurrentTime(dto.getCurrentTime());
        room.setPlaying(dto.isPlaying());
        room.setLastUpdated(dto.getLastUpdated());

        saveRoomInfo(dto.getRoomId().toString(), room);
    }

    public void addRoomToArtist(String artistId, String roomId) {
        String key = "artist:" + artistId + ":rooms";
        redisTemplate.opsForSet().add(key, roomId);
    }

    public LiveRoomDTO getRoomInfo(String roomId) {
        String key = "room:" + roomId + ":info";
        Map<Object, Object> map = redisTemplate.opsForHash().entries(key);

        if (map.isEmpty()) {
            throw new CustomException("Redis에 해당 roomId의 방 정보가 존재하지 않습니다.", HttpStatus.NOT_FOUND);
        }

        // artistId 꺼내서 Long으로 변환
        Object artistIdObj = map.get("artistId");
        Long artistId = null;
        if (artistIdObj != null) {
            // Redis에 저장된 값이 "\"123\"" 같은 형태일 수 있으니, toString() 후 숫자만 파싱
            String artistIdStr = artistIdObj.toString().replace("\"", "");
            artistId = Long.valueOf(artistIdStr);
        }

        return LiveRoomDTO.builder()
                .roomId(Long.valueOf((roomId)))
                .title((String) map.get("title"))
                .artistId(artistId)
                .hostId((String) map.get("hostId"))
                .imgUrl((String) map.get("imgUrl"))
                .playlist((List<String>) map.get("playlist"))
                .currentVideoIndex((int) map.get("currentVideoIndex"))
                .currentTime((double) map.get("currentTime"))
                .playing((boolean) map.get("playing"))
                .lastUpdated((long) map.get("lastUpdated"))
                .isLocked(Boolean.parseBoolean(map.getOrDefault("isLocked", "false").toString()))
                .entryQuestion((String) map.getOrDefault("entryQuestion", null))
                .entryAnswer((String) map.getOrDefault("entryAnswer", null))
                .build();
    }



    public void deleteRoomInfo(Long artistId, Long roomId) {
        String roomKey = "room:" + roomId + ":info";
        String artistRoomsKey = "artist:" + artistId + ":rooms";
        String roomUsersKey = "room:" + roomId + ":users";

        Boolean deleted = redisTemplate.delete(roomKey);
        Long removed = redisTemplate.opsForSet().remove(artistRoomsKey, roomId.toString());
        Boolean deletedUsers = redisTemplate.delete(roomUsersKey);

        if (deleted != null && deleted == false) {
            throw new CustomException("roomId에 대한 방이 존재하지 않습니다.", HttpStatus.NOT_FOUND);
        }

        if (removed == 0) {
            throw new CustomException("해당 아티스트의 방 목록에 해당 roomId가 존재하지 않습니다.", HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public void addUserToRoom(String roomId, User user) {
        String key = "room:" + roomId + ":users";
        String userInfo = user.getUserId();

        redisTemplate.opsForSet().add(key, userInfo);
        redisTemplate.expire(key, Duration.ofHours(6));
    }

    @Override
    public void removeUserFromRoom(String artistId, String roomId, User user){
        String userSetKey = "room:" + roomId + ":users";
        String roomInfoKey = "room:" + roomId + ":info";
        String artistRoomsKey = "artist:" + artistId + ":rooms";

        String userInfo = user.getUserId();

        Set<String> userSetBeforeRemoval = redisTemplate.opsForSet()
                .members(userSetKey)
                .stream().map(Object::toString).collect(Collectors.toSet());

        Map<Object, Object> backupRoomInfo = redisTemplate.opsForHash().entries(roomInfoKey);
        boolean wasInArtistRooms = redisTemplate.opsForSet().isMember(artistRoomsKey, roomId);

        // 유저 제거
        redisTemplate.opsForSet().remove(userSetKey, userInfo);

        Long size = redisTemplate.opsForSet().size(userSetKey);
        if (size != null && size == 0L) {

            Boolean deleted = redisTemplate.delete(userSetKey);

            if (deleted == null) {
                throw new CustomException("접속 유저 목록 삭제 중 오류", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            boolean deletedRoomInfo = redisTemplate.delete(roomInfoKey);
            if (!deletedRoomInfo) {
                // 롤백
                if (!userSetBeforeRemoval.isEmpty()) {
                    redisTemplate.opsForSet().add(userSetKey, userSetBeforeRemoval.toArray());
                }
                throw new CustomException("방 정보 삭제 실패", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            Long removed = redisTemplate.opsForSet().remove(artistRoomsKey, roomId);
            if (removed == null || removed == 0) {
                // 롤백
                if (!userSetBeforeRemoval.isEmpty()) {
                    redisTemplate.opsForSet().add(userSetKey, userSetBeforeRemoval.toArray());
                }
                if (!backupRoomInfo.isEmpty()) {
                    redisTemplate.opsForHash().putAll(roomInfoKey, backupRoomInfo);
                }
                if (wasInArtistRooms) {
                    redisTemplate.opsForSet().add(artistRoomsKey, roomId);
                }
                throw new CustomException("아티스트 방 목록에서 제거 실패", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }else if (size != null && size > 0L) {
            String newHostId = redisTemplate.opsForSet().members(userSetKey)
                    .stream()
                    .map(Object::toString)
                    .findFirst()
                    .orElse(null);

            if (newHostId != null) {
                redisTemplate.opsForHash().put(roomInfoKey, "hostId", newHostId);
            }

        }
    }

    @Override
    public List<LiveRoomSummaryDTO> getAllRoomSummaries(Long artistId) {
        String artistRoomsKey = "artist:" + artistId + ":rooms";
        Set<Object> roomIds = redisTemplate.opsForSet().members(artistRoomsKey);
        if (roomIds == null || roomIds.isEmpty()) {
            return List.of();
        }

        return roomIds.stream()
                .map(Object::toString)
                .map(roomId -> {
                    String roomInfoKey = "room:" + roomId + ":info";
                    Map<Object, Object> roomMap = redisTemplate.opsForHash().entries(roomInfoKey);
                    if (roomMap.isEmpty()) return null;

                    // 참가자 수 조회
                    Long userCount = redisTemplate.opsForSet().size("room:" + roomId + ":users");
                    int participantCount = userCount != null ? userCount.intValue() : 0;

                    String hostUserId = (String) roomMap.get("hostId");
                    String title      = (String) roomMap.get("title");
                    String imgUrl     = (String) roomMap.get("imgUrl");

                    // hostId로 User 조회 (null 체크)
                    User host = userRepository.findByUserId(hostUserId);
                    String hostNickname     = host != null ? host.getNickname() : null;
                    String hostProfileImage = host != null ? host.getImgUrl()    : null;

                    return LiveRoomSummaryDTO.builder()
                            .roomId(Long.valueOf(roomId))
                            .title(title)
                            .hostId(hostUserId)
                            .hostNickname(hostNickname)
                            .hostProfileImgUrl(hostProfileImage)
                            .imgUrl(imgUrl)
                            .participantCount(participantCount)
                            .build();
                })
                .filter(Objects::nonNull)
                .toList();
    }

    /** 전체 방 중 참여자 수 기준 Top-N을 TrendingRoomDTO로 반환 */
    @Override
    public List<TrendingRoomDTO> getTrendingRooms(int size) {
        Set<String> keys = redisTemplate.keys("room:*:info");
        if (keys == null || keys.isEmpty()) {
            return List.of();
        }

        return keys.stream()
                .map(key -> {
                    // key = "room:<roomId>:info"
                    String[] parts = key.split(":");
                    if (parts.length < 2) return null;
                    String roomId = parts[1];

                    Map<Object, Object> m = redisTemplate.opsForHash().entries(key);
                    if (m.isEmpty()) return null;

                    // 1) participant count
                    Long cnt = redisTemplate.opsForSet().size("room:" + roomId + ":users");
                    int participantCount = cnt != null ? cnt.intValue() : 0;

                    // 2) artistId guard
                    Object artistIdObj = m.get("artistId");
                    if (artistIdObj == null) {
                        // artistId 정보가 없으면 이 방은 스킵
                        return null;
                    }
                    Long artistId;
                    try {
                        artistId = Long.valueOf(artistIdObj.toString());
                    } catch (NumberFormatException e) {
                        return null;
                    }

                    // 3) 기본 정보
                    String title  = (String) m.get("title");
                    String imgUrl = (String) m.get("imgUrl");
                    String hostUid= (String) m.get("hostId");

                    // 4) host 정보
                    User host = userRepository.findByUserId(hostUid);
                    String hostNickname   = host != null ? host.getNickname() : null;
                    String hostProfileImg = host != null ? host.getImgUrl()    : null;

                    // 5) artist 정보
                    Artist artist = artistRepository.findById(artistId).orElse(null);
                    String artistNameEn = artist != null ? artist.getNameEn() : null;
                    String artistNameKr = artist != null ? artist.getNameKr() : null;

                    return TrendingRoomDTO.builder()
                            .roomId(Long.valueOf(roomId))
                            .artistId(artistId)
                            .artistNameEn(artistNameEn)
                            .artistNameKr(artistNameKr)
                            .title(title)
                            .hostId(hostUid)
                            .hostNickname(hostNickname)
                            .hostProfileImgUrl(hostProfileImg)
                            .imgUrl(imgUrl)
                            .participantCount(participantCount)
                            .build();
                })
                .filter(Objects::nonNull)
                // 참여자 많은 순으로 정렬
                .sorted(Comparator.comparingInt(TrendingRoomDTO::getParticipantCount).reversed())
                // Top-N, 방 개수가 N보다 적으면 있는 만큼만 반환
                .limit(size)
                .toList();
    }


}
