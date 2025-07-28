package com.a404.duckonback.service;

import com.a404.duckonback.entity.Artist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ArtistService {
    List<Integer> findAllArtistIdByUserId(Long id);
    void followArtists(Long id, List<Integer> artistList);
    Page<Artist> getArtists(Pageable pageable);
    List<Artist> searchArtists(String keyword);
}
