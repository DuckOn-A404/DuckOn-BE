package com.a404.duckonback.service;

import java.util.List;

public interface ArtistService {
    List<Integer> findAllArtistIdByUserId(Long id);
    void followArtists(Long id, List<Integer> artistList);
}
