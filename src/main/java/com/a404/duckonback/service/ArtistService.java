package com.a404.duckonback.service;

import java.util.List;

public interface ArtistService {
    List<Integer> findAllArtistIdByUserUuid(String uuid);
    void followArtists(String uuid, List<Integer> artistList);
}
