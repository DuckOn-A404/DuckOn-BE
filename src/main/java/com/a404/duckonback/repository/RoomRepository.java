package com.a404.duckonback.repository;

import com.a404.duckonback.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> findByCreator_Id(Long id);            // 해당 유저가 만든 방
    List<Room> findBySubject_Id(Long subjectId);     // 특정 Subject 방
}
