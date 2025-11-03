package com.a404.duckonback.service;

import com.a404.duckonback.entity.Room;

import java.util.List;
import java.util.Optional;

public interface RoomService {
    Room createRoom(Room room);
    Optional<Room> getRoomById(Long roomId);
    List<Room> getAllRooms();
    Room updateRoom(Long roomId, Room updatedRoom);
    void deleteRoom(Long roomId);
    List<Room> getRoomsByArtist(Long artistId);
}
