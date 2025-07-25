package com.a404.duckonback.service;

import com.a404.duckonback.entity.Room;

import java.util.List;
import java.util.Optional;

public interface RoomService {
    Room createRoom(Room room);
    Optional<Room> getRoomById(Integer roomId);
    List<Room> getAllRooms();
    Room updateRoom(Integer roomId, Room updatedRoom);
    void deleteRoom(Integer roomId);
    List<Room> getRoomsByCreator(Long id);
    List<Room> getRoomsByArtist(Integer artistId);
}
