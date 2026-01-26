package com.a404.duckonback.domain.artist.artist.service;

import com.a404.duckonback.domain.admin.dto.AdminArtistPatchDTO;
import com.a404.duckonback.domain.admin.dto.AdminArtistRequestDTO;
import com.a404.duckonback.domain.artist.artist.dto.ArtistDTO;
import com.a404.duckonback.domain.artist.artist.dto.ArtistDetailDTO;
import com.a404.duckonback.domain.artist.artist.entity.Artist;
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

    Page<ArtistDTO> getArtists(Pageable pageable, String sort, String order, String keyword);

}
