package com.a404.duckonback.domain.artist.emerging.service;

import com.a404.duckonback.domain.artist.emerging.dto.EmergingArtistCreateRequest;
import com.a404.duckonback.domain.artist.emerging.dto.EmergingArtistCreateResponse;

public interface EmergingArtistService {
    EmergingArtistCreateResponse create(Long userId, EmergingArtistCreateRequest emergingArtistCreateRequest);
}
