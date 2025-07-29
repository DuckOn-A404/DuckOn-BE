package com.a404.duckonback.service;

import com.a404.duckonback.dto.LiveRoomDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
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
}
