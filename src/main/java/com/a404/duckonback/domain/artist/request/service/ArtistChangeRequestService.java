package com.a404.duckonback.domain.artist.request.service;

import com.a404.duckonback.common.dto.PageResponse;
import com.a404.duckonback.domain.artist.request.dto.*;

public interface ArtistChangeRequestService {
    void create(Long userId, ArtistChangeRequestCreateRequestDTO req);
    PageResponse<ArtistChangeRequestInfoDTO> getMyRequests(int page, int size, long userId);
    PageResponse<ArtistChangeRequestAdminInfoDTO> getAllRequests(int page, int size);
    ArtistChangeRequestAdminDetailInfoDTO getDetail(Long requestId);
    void reviewRequest(Long requestId, Long adminId, ArtistChangeRequestAdminReviewRequestDTO approveRequestDTO);
}
