package com.a404.duckonback.domain.artist.emerging.service;

import com.a404.duckonback.common.dto.PageResponse;
import com.a404.duckonback.domain.artist.emerging.dto.EmergingArtistCreateRequestDTO;
import com.a404.duckonback.domain.artist.emerging.dto.EmergingArtistCreateResponseDTO;
import com.a404.duckonback.domain.artist.emerging.dto.EmergingArtistDetailResponseDTO;
import com.a404.duckonback.domain.artist.emerging.dto.EmergingArtistListResponseDTO;

import java.util.List;

public interface EmergingArtistService {
    EmergingArtistCreateResponseDTO create(Long userId, EmergingArtistCreateRequestDTO emergingArtistCreateRequestDTO);
    PageResponse<EmergingArtistListResponseDTO> getList(int page, int size, String sort, String order, String keyword);
    EmergingArtistDetailResponseDTO getEmergingArtistDetail(Long emergingArtistId, Long userId);
    List<EmergingArtistListResponseDTO> getRandomEmergingArtistList(int count);
}
