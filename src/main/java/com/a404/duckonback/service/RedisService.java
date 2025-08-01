package com.a404.duckonback.service;

import com.a404.duckonback.dto.LiveRoomDTO;
import com.a404.duckonback.entity.User;

public interface RedisService {
    void saveRoomInfo(String roomId, LiveRoomDTO room);
    void addRoomToArtist(String artistId, String roomId);
    LiveRoomDTO getRoomInfo(String roomId);
    void deleteRoomInfo(Long artistId, Long roomId);
    void addUserToRoom(String roomId, User user);
    void removeUserFromRoom(String artistId, String roomId,User user);
}
