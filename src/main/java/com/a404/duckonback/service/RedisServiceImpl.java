package com.a404.duckonback.service;

import com.a404.duckonback.dto.LiveRoomDTO;
import com.a404.duckonback.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RedisServiceImpl implements RedisService {
    private final RedisTemplate<String, Object> redisTemplate;

    public void saveRoomInfo(String roomId, LiveRoomDTO room) {
        String key = "room:" + roomId + ":info";

        Map<String, Object> map = Map.of(
                "title", room.getTitle(),
                "hostId", room.getHostId(),
                "imgUrl", room.getImgUrl(),
                "playlist", room.getPlaylist(),
                "currentVideoIndex", room.getCurrentVideoIndex(),
                "currentTime", room.getCurrentTime(),
                "isPlaying", room.isPlaying(),
                "lastUpdated", room.getLastUpdated()
        );

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

        if (map.isEmpty()) return null;

        return LiveRoomDTO.builder()
                .title((String) map.get("title"))
                .hostId((String) map.get("hostId"))
                .imgUrl((String) map.get("imgUrl"))
                .playlist((List<String>) map.get("playlist"))
                .currentVideoIndex((int) map.get("currentVideoIndex"))
                .currentTime((double) map.get("currentTime"))
                .isPlaying((boolean) map.get("isPlaying"))
                .lastUpdated((long) map.get("lastUpdated"))
                .build();
    }

    public void deleteRoomInfo(Long artistId, Long roomId) {
        String roomKey = "room:" + roomId + ":info";
        String artistRoomsKey = "artist:" + artistId + ":rooms";

        Boolean deleted = redisTemplate.delete(roomKey);
        Long removed = redisTemplate.opsForSet().remove(artistRoomsKey, roomId.toString());

        if (deleted != null && deleted == false) {
            throw new CustomException("roomId에 대한 방이 존재하지 않습니다.", HttpStatus.NOT_FOUND);
        }

        if (removed == 0) {
            throw new CustomException("해당 아티스트의 방 목록에 해당 roomId가 존재하지 않습니다.", HttpStatus.NOT_FOUND);
        }
    }
}
