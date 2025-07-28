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
    List<ArtistFollow> findByUser_Id(Long id);
    List<ArtistFollow> findByArtist_ArtistId(Integer artistId);
    boolean existsByUser_IdAndArtist_ArtistId(Long id, Integer artistId);
    void deleteByUser_IdAndArtist_ArtistId(Long id, Integer artistId);
    
    // 특정 아티스트 팔로워 수 조회
    long countByArtist_ArtistId(Integer artistId);
}
