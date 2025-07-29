package com.a404.duckonback.service;

import com.a404.duckonback.dto.ArtistDTO;
import com.a404.duckonback.entity.Artist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ArtistService {

    List<Long> findAllArtistIdByUserId(Long id);

    Page<ArtistDTO> getArtists(Pageable pageable);
    List<ArtistDTO> searchArtists(String keyword);
    List<ArtistDTO> getRandomArtists(int size);

}
