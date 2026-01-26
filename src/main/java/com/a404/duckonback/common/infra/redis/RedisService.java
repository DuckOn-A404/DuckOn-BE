package com.a404.duckonback.common.infra.redis;

import com.a404.duckonback.domain.room.dto.*;
import com.a404.duckonback.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

public interface RedisService {
    void saveRoomInfo(String roomId, LiveRoomDTO room);
    void addRoomToArtist(String artistId, String roomId);
    LiveRoomDTO getRoomInfo(String roomId);
    void deleteRoomInfo(Long artistId, Long roomId);
    void addUserToRoom(String roomId, User user);
    void removeUserFromRoom(String artistId, String roomId,String userId);
    List<LiveRoomSummaryDTO> getAllRoomSummaries(Long artistId);
    List<HomeArtistRoomDTO> getHomeArtistRooms(List<Long> artistIds, int roomLimitPerArtist);
    List<RoomListInfoDTO> getTrendingRooms(int size);        // 기존
    Page<RoomListInfoDTO> getTrendingRooms(Pageable pageable); // 페이징 추가

    void updateRoomInfo(LiveRoomSyncDTO room);
    Long getRoomUserCount(String roomId);

    boolean increaseChatCount(String roomId, String userId);

    RoomListInfoDTO getActiveRoomByHost(String hostUserId);
    void addParticipantCountToRoom(String roomId);
    void decreaseParticipantCountFromRoom(String roomId);
    boolean isUserBanned(String roomId, String userId);
    boolean acquireCreateRoomLock(String hostUserId);
    void releaseCreateRoomLock(String hostUserId);
    Set<String> scanKeys(String pattern);
}
