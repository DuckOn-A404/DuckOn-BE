package com.a404.duckonback.domain.artist.emerging.repository;

import com.a404.duckonback.domain.artist.emerging.entity.EmergingArtist;
import com.a404.duckonback.domain.artist.emerging.entity.EmergingArtistFollow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmergingArtistFollowRepository extends JpaRepository<EmergingArtistFollow,Long> {
    EmergingArtistFollow findByUser_IdAndEmergingArtist(Long userId, EmergingArtist emergingArtist);
    void deleteByUser_IdAndEmergingArtist(Long userId, EmergingArtist emergingArtist);
    long countByEmergingArtist(EmergingArtist emergingArtist);
}
