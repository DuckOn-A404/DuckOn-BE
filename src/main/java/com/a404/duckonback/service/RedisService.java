package com.a404.duckonback.service;

import com.a404.duckonback.dto.LiveRoomDTO;
import com.a404.duckonback.dto.LiveRoomSummaryDTO;
import com.a404.duckonback.dto.LiveRoomSyncDTO;
import com.a404.duckonback.dto.RoomListInfoDTO;
import com.a404.duckonback.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface RedisService {
    void saveRoomInfo(String roomId, LiveRoomDTO room);
    void addRoomToArtist(String artistId, String roomId);
    LiveRoomDTO getRoomInfo(String roomId);
    void deleteRoomInfo(Long artistId, Long roomId);
    void addUserToRoom(String roomId, User user);
    void removeUserFromRoom(String artistId, String roomId,User user);
    List<LiveRoomSummaryDTO> getAllRoomSummaries(Long artistId);
    List<RoomListInfoDTO> getTrendingRooms(int size);        // 기존
    Page<RoomListInfoDTO> getTrendingRooms(Pageable pageable); // 페이징 추가

    void updateRoomInfo(LiveRoomSyncDTO room);
    Long getRoomUserCount(String roomId);
}
