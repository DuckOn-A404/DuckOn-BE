package com.a404.duckonback.service;

import com.a404.duckonback.entity.Room;
import com.a404.duckonback.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;

    @Override
    public Room createRoom(Room room) {
        return roomRepository.save(room);
    }

    @Override
    public Optional<Room> getRoomById(Long roomId) {
        return roomRepository.findById(roomId);
    }

    @Override
    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    @Override
    public Room updateRoom(Long roomId, Room updatedRoom) {
        return roomRepository.findById(roomId)
                .map(room -> {
                    room.setTitle(updatedRoom.getTitle());
                    room.setImgUrl(updatedRoom.getImgUrl());
                    room.setCreatedAt(updatedRoom.getCreatedAt());
                    room.setCreator(updatedRoom.getCreator());
                    room.setArtist(updatedRoom.getArtist());
                    return roomRepository.save(room);
                })
                .orElseThrow(() -> new IllegalArgumentException("Room not found with ID: " + roomId));
    }

    @Override
    public void deleteRoom(Long roomId) {
        roomRepository.deleteById(roomId);
    }

    @Override
    public List<Room> getRoomsByCreator(Long id) {
        return roomRepository.findByCreator_Id(id);
    }

    @Override
    public List<Room> getRoomsByArtist(Long artistId) {
        return roomRepository.findByArtist_ArtistId(artistId);
    }
}
