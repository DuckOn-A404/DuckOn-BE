package com.a404.duckonback.repository;

import com.a404.duckonback.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> findByCreator_Id(Long id);       // 해당 유저가 만든 방
    List<Room> findByArtist_ArtistId(Long artistId); // 특정 아티스트 방
}
