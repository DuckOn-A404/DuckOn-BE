package com.a404.duckonback.service;

import com.a404.duckonback.dto.LiveRoomDTO;
import com.a404.duckonback.dto.LiveRoomSummaryDTO;
import com.a404.duckonback.dto.LiveRoomSyncDTO;
import com.a404.duckonback.dto.TrendingRoomDTO;
import com.a404.duckonback.entity.User;

import java.util.List;

public interface RedisService {
    void saveRoomInfo(String roomId, LiveRoomDTO room);
    void addRoomToArtist(String artistId, String roomId);
    LiveRoomDTO getRoomInfo(String roomId);
    void deleteRoomInfo(Long artistId, Long roomId);
    void addUserToRoom(String roomId, User user);
    void removeUserFromRoom(String artistId, String roomId,User user);
    List<LiveRoomSummaryDTO> getAllRoomSummaries(Long artistId);
    List<TrendingRoomDTO> getTrendingRooms(int size);

    void updateRoomInfo(LiveRoomSyncDTO room);
    Long getRoomUserCount(String roomId);
}
