package com.a404.duckonback.service;

import com.a404.duckonback.dto.CreateRoomRequestDTO;
import com.a404.duckonback.dto.LiveRoomDTO;

public interface LiveRoomService {
    LiveRoomDTO createRoom(CreateRoomRequestDTO req);
    LiveRoomDTO getRoom(String roomId);
}
