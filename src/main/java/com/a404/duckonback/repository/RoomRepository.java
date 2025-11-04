package com.a404.duckonback.repository;

import com.a404.duckonback.dto.LiveRoomSummaryDTO;
import com.a404.duckonback.dto.RoomDTO;
import com.a404.duckonback.dto.RoomSummaryDTO;
import com.a404.duckonback.entity.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    Page<Room> findByCreator_IdOrderByCreatedAtDesc(Long creatorId, Pageable pageable);
    @Query("""
            SELECT new com.a404.duckonback.dto.RoomSummaryDTO(
                        r.roomId, r.title, r.imgUrl, r.createdAt
            )
            FROM Room r
            WHERE r.creator.id = :creatorId
            ORDER BY r.createdAt DESC
            """)
    Page<RoomSummaryDTO> findRoomSummariesByCreatorIdOrderByCreatedAtDesc(@Param("creatorId") Long creatorId, Pageable pageable);

    long countByCreator_Id(Long creatorId);    // 해당 유저가 만든 방 개수

    List<Room> findByArtist_ArtistId(Long artistId); // 특정 아티스트 방

    // 리더보드 쿼리
    @Query(value = """
        SELECT r.creator_id AS id, COUNT(*) AS cnt
        FROM room r
        GROUP BY r.creator_id
        ORDER BY cnt DESC, id ASC
        LIMIT :limit OFFSET :offset
    """, nativeQuery = true)
    List<Object[]> findLeaderboard(@Param("limit") int limit, @Param("offset") int offset);
}
