package com.a404.duckonback.repository;

import com.a404.duckonback.entity.Artist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArtistRepository extends JpaRepository<Artist, Integer> {

    @Query("SELECT af.artist.artistId FROM ArtistFollow af WHERE af.user.id = :id")
    List<Integer> findAllArtistIdByUserId(@Param("id") Long id);

    @Query("""
        SELECT a FROM Artist a 
        WHERE LOWER(a.nameKr) LIKE LOWER(CONCAT('%', :keyword, '%')) 
           OR LOWER(a.nameEn) LIKE LOWER(CONCAT('%', :keyword, '%'))
        """)
    List<Artist> searchByKeyword(@Param("keyword") String keyword);

    // size만큼 랜덤으로 아티스트 반환 (MySQL 기준)
    @Query(
            value = "SELECT * FROM artist ORDER BY RAND() LIMIT :size",
            nativeQuery = true
    )
    List<Artist> findRandomArtists(@Param("size") int size);

}
