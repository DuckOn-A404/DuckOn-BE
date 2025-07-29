package com.a404.duckonback.repository;

import com.a404.duckonback.entity.ArtistFollow;
import com.a404.duckonback.entity.ArtistFollowId;

import com.a404.duckonback.entity.User;
import com.a404.duckonback.entity.Artist;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArtistFollowRepository extends JpaRepository<ArtistFollow, ArtistFollowId> {
    List<ArtistFollow> findByUser_Id(Long id);
    List<ArtistFollow> findByArtist_ArtistId(Long artistId);
    boolean existsByUser_IdAndArtist_ArtistId(Long id, Long artistId);
    void deleteByUser_IdAndArtist_ArtistId(Long id, Long artistId);
    
    // 특정 아티스트 팔로워 수 조회
    long countByArtist_ArtistId(Long artistId);
    // 페이징을 위한 쿼리 메서드 추가
    Page<ArtistFollow> findByUser_Id(Long userId, Pageable pageable);
}
