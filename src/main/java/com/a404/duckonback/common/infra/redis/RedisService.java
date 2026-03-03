package com.a404.duckonback.common.infra.redis;

import com.a404.duckonback.domain.room.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

public interface RedisService {
    void saveRoomInfo(String roomId, LiveRoomDTO room);
    void addRoomToArtist(String artistId, String roomId);
    LiveRoomDTO getRoomInfo(String roomId);
    void deleteRoomInfo(Long artistId, Long roomId);
    List<LiveRoomSummaryDTO> getAllRoomSummaries(Long artistId);
    List<HomeArtistRoomDTO> getHomeArtistRooms(List<Long> artistIds, int roomLimitPerArtist);
    List<RoomListInfoDTO> getTrendingRooms(int size);        // 기존
    Page<RoomListInfoDTO> getTrendingRooms(Pageable pageable); // 페이징 추가

    void updateRoomInfo(LiveRoomSyncDTO room);
    Long getRoomUserCount(String roomId);

    boolean increaseChatCount(String roomId, String userId);

    RoomListInfoDTO getActiveRoomByHost(String hostUserId);
    boolean isUserBanned(String roomId, String userId);
    boolean acquireCreateRoomLock(String hostUserId);
    void releaseCreateRoomLock(String hostUserId);
    Set<String> scanKeys(String pattern);

    String getOrCreateGuestNickname(String guestId);

    boolean addViewerToRoom(String roomId, String viewerId);
    boolean removeViewerFromRoom(String artistId, String roomId, String viewerId);

    Long getRoomUserCountStrict(String roomId);
}
