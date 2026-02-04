package com.a404.duckonback.domain.artist.emerging.repository;

import com.a404.duckonback.domain.artist.emerging.entity.EmergingArtist;
import com.a404.duckonback.domain.artist.emerging.entity.EmergingArtistFollow;
import com.a404.duckonback.domain.artist.emerging.entity.EmergingArtistFollowId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmergingArtistFollowRepository extends JpaRepository<EmergingArtistFollow, EmergingArtistFollowId> {
    Optional<EmergingArtistFollow> findByUser_IdAndEmergingArtist(Long userId, EmergingArtist emergingArtist);
    void deleteByUser_IdAndEmergingArtist(Long userId, EmergingArtist emergingArtist);
    long countByEmergingArtist(EmergingArtist emergingArtist);
    List<EmergingArtistFollow> findAllByUser_Id(Long userId);

    @Query("""
        select
            ea.emergingArtistId as emergingArtistId,
            f.createdAt as createdAt,
            ea.debutDate as debutDate,
            ea.nameKr as nameKr,
            ea.nameEn as nameEn,
            ea.imgUrl as imgUrl,
            ea.status as status,
            (
                select count(f2)
                from EmergingArtistFollow f2
                where f2.emergingArtist = ea
            ) as followerCount
        from EmergingArtistFollow f
        join f.emergingArtist ea
        where f.user.id = :userId
        order by f.createdAt desc
        """)
    Page<FollowedEmergingArtistRow> findFollowedArtists(@Param("userId") Long userId, Pageable pageable);
}
