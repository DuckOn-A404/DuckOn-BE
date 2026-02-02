package com.a404.duckonback.domain.artist.request.repository;

import com.a404.duckonback.domain.artist.request.entity.ArtistProfileChangeRequest;
import com.a404.duckonback.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ArtistChangeRequestRepository extends JpaRepository<ArtistProfileChangeRequest, Long> {

    Page<ArtistProfileChangeRequest> findByRequestedBy_IdOrderByCreatedAtDesc(Long requestedById, Pageable pageable);

}
