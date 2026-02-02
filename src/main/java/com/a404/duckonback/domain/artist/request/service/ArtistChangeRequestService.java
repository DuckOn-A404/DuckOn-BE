package com.a404.duckonback.domain.artist.request.service;

import com.a404.duckonback.domain.artist.request.dto.ArtistChangeRequestCreateRequestDTO;

public interface ArtistChangeRequestService {
    void create(Long userId, ArtistChangeRequestCreateRequestDTO req);
}
