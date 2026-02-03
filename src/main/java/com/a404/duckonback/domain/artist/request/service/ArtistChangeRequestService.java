package com.a404.duckonback.domain.artist.request.service;

import com.a404.duckonback.common.dto.PageResponse;
import com.a404.duckonback.domain.artist.request.dto.ArtistChangeRequestAdminDetailInfoDTO;
import com.a404.duckonback.domain.artist.request.dto.ArtistChangeRequestAdminInfoDTO;
import com.a404.duckonback.domain.artist.request.dto.ArtistChangeRequestCreateRequestDTO;
import com.a404.duckonback.domain.artist.request.dto.ArtistChangeRequestInfoDTO;

public interface ArtistChangeRequestService {
    void create(Long userId, ArtistChangeRequestCreateRequestDTO req);
    PageResponse<ArtistChangeRequestInfoDTO> getMyRequests(int page, int size, long userId);
    PageResponse<ArtistChangeRequestAdminInfoDTO> getAllRequests(int page, int size);
    ArtistChangeRequestAdminDetailInfoDTO getDetail(Long requestId);
}
