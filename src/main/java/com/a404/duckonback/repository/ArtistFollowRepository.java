package com.a404.duckonback.repository;

import com.a404.duckonback.entity.ArtistFollow;
import com.a404.duckonback.entity.ArtistFollowId;

import com.a404.duckonback.entity.User;
import com.a404.duckonback.entity.Artist;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArtistFollowRepository extends JpaRepository<ArtistFollow, ArtistFollowId> {
    List<ArtistFollow> findByUser_Uuid(String uuid);
    List<ArtistFollow> findByArtist_ArtistId(Integer artistId);
    boolean existsByUser_UuidAndArtist_ArtistId(String uuid, Integer artistId);
    void deleteByUser_UuidAndArtist_ArtistId(String uuid, Integer artistId);
  
}
