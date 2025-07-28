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

}
