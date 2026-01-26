package com.a404.duckonback.domain.artist.emerging.repository;

import com.a404.duckonback.domain.artist.emerging.entity.EmergingArtist;
import com.a404.duckonback.domain.artist.emerging.entity.EmergingArtistStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmergingArtistRepository extends JpaRepository<EmergingArtist, Long> {
    boolean existsByNameKrAndNameEnAndStatus(String nameKr, String nameEn, EmergingArtistStatus status);
}
