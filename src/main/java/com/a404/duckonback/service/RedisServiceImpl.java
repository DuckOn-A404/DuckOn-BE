package com.a404.duckonback.service;

import com.a404.duckonback.dto.LiveRoomDTO;
import com.a404.duckonback.dto.LiveRoomSummaryDTO;
import com.a404.duckonback.entity.User;
import com.a404.duckonback.exception.CustomException;
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

    public void saveRoomInfo(String roomId, LiveRoomDTO room) {
        String key = "room:" + roomId + ":info";

        Map<String, Object> map = new HashMap<>();
        map.put("title", room.getTitle());
        map.put("hostId", room.getHostId());
        map.put("imgUrl", room.getImgUrl());
        map.put("playlist", room.getPlaylist());
        map.put("currentVideoIndex", room.getCurrentVideoIndex());
        map.put("currentTime", room.getCurrentTime());
        map.put("isPlaying", room.isPlaying());
        map.put("lastUpdated", room.getLastUpdated());
        map.put("isLocked", room.isLocked());
        map.put("entryQuestion", room.getEntryQuestion());
        map.put("entryAnswer", room.getEntryAnswer());


        redisTemplate.opsForHash().putAll(key, map);
        redisTemplate.expire(key, Duration.ofHours(6));
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


        return LiveRoomDTO.builder()
                .roomId(Long.valueOf((roomId)))
                .title((String) map.get("title"))
                .hostId((String) map.get("hostId"))
                .imgUrl((String) map.get("imgUrl"))
                .playlist((List<String>) map.get("playlist"))
                .currentVideoIndex((int) map.get("currentVideoIndex"))
                .currentTime((double) map.get("currentTime"))
                .isPlaying((boolean) map.get("isPlaying"))
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

                    String userSetKey = "room:" + roomId + ":users";
                    Long userCount = redisTemplate.opsForSet().size(userSetKey);
                    int participantCount = userCount != null ? userCount.intValue() : 0;

                    return LiveRoomSummaryDTO.builder()
                            .roomId(Long.valueOf(roomId))
                            .title((String) roomMap.get("title"))
                            .hostId((String) roomMap.get("hostId"))
                            .imgUrl((String) roomMap.get("imgUrl"))
                            .participantCount(participantCount)
                            .build();
                })
                .filter(Objects::nonNull)
                .toList();
    }


}
