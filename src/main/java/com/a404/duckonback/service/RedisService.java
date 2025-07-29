package com.a404.duckonback.service;

import com.a404.duckonback.dto.LiveRoomDTO;

public interface RedisService {
    void saveRoomInfo(String roomId, LiveRoomDTO room);
    void addRoomToArtist(String artistId, String roomId);
    LiveRoomDTO getRoomInfo(String roomId);
}
