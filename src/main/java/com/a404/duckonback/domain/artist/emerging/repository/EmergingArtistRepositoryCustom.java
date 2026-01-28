package com.a404.duckonback.domain.artist.emerging.repository;

import com.a404.duckonback.common.enums.SortOrder;
import com.a404.duckonback.domain.artist.emerging.dto.EmergingArtistListResponseDTO;
import com.a404.duckonback.domain.artist.emerging.entity.EmergingArtistSort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EmergingArtistRepositoryCustom {
    Page<EmergingArtistListResponseDTO> pageEmergingArtists(
            Pageable pageable,
            EmergingArtistSort sort,
            SortOrder order,
            String keyword
    );
}
