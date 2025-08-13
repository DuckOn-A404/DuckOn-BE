package com.a404.duckonback.service;

import com.a404.duckonback.dto.AdminArtistPatchDTO;
import com.a404.duckonback.dto.AdminArtistRequestDTO;
import com.a404.duckonback.dto.ArtistDTO;
import com.a404.duckonback.dto.ArtistDetailDTO;
import com.a404.duckonback.entity.Artist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ArtistService {

    Artist findById(Long artistId);
    List<Long> findAllArtistIdByUserId(Long id);

    ArtistDetailDTO getArtistDetail(Long userId, Long artistId);
    Page<ArtistDTO> getArtists(Pageable pageable);
    List<ArtistDTO> searchArtists(String keyword);
    List<ArtistDTO> getRandomArtists(int size);

    Artist createArtist(AdminArtistRequestDTO dto);
    Artist updateArtist(Long artistId, AdminArtistRequestDTO dto);
    Artist patchArtist(Long artistId, AdminArtistPatchDTO dto);

    String findSlugById(Long artistId);

}
