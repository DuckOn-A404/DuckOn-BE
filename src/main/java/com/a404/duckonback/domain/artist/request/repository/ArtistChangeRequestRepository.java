package com.a404.duckonback.domain.artist.request.repository;

import com.a404.duckonback.domain.artist.request.entity.ArtistProfileChangeRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArtistChangeRequestRepository extends JpaRepository<ArtistProfileChangeRequest, Long> {
}
