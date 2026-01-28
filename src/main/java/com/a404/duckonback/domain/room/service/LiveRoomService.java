package com.a404.duckonback.domain.room.service;

import com.a404.duckonback.domain.room.dto.CreateRoomRequestDTO;
import com.a404.duckonback.domain.room.dto.LiveRoomDTO;

public interface LiveRoomService {
    LiveRoomDTO createRoom(CreateRoomRequestDTO req);
    boolean hasActiveRoomByHost(String hostUserId);

}
