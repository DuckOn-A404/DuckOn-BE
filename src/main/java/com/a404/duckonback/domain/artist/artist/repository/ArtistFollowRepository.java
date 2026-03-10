package com.a404.duckonback.domain.artist.artist.repository;

import com.a404.duckonback.domain.artist.artist.entity.ArtistFollow;
import com.a404.duckonback.domain.artist.artist.entity.ArtistFollowId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArtistFollowRepository extends JpaRepository<ArtistFollow, ArtistFollowId> {
    List<ArtistFollow> findByUser_Id(Long id);
    List<ArtistFollow> findByArtist_ArtistId(Long artistId);
    boolean existsByUser_IdAndArtist_ArtistId(Long id, Long artistId);
    void deleteByUser_IdAndArtist_ArtistId(Long id, Long artistId);

    Optional<ArtistFollow> findByUser_IdAndArtist_ArtistId(Long userId, Long artistId);

    // 특정 아티스트 팔로워 수 조회
    long countByArtist_ArtistId(Long artistId);
    // 페이징을 위한 쿼리 메서드 추가
    Page<ArtistFollow> findByUser_Id(Long userId, Pageable pageable);
    
    // 팔로우 중인 아티스트 이름 목록 조회
    @Query("SELECT af.artist.name FROM ArtistFollow af WHERE af.user.id = :userId")
    List<String> findArtistNamesByUserId(@Param("userId") Long userId);
}
