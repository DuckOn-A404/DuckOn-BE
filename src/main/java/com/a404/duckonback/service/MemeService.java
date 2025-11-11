package com.a404.duckonback.service;

import com.a404.duckonback.dto.*;

import java.util.List;

public interface MemeService {
    MemeCreateResponseDTO createMemes(Long userId, MemeCreateRequestDTO request);
    RandomMemeResponseDTO getRandomMemes(int page, int size);
    void createFavorite(Long userId, Long memeId);
    void deleteFavorite(Long userId, Long memeId);
    List<FavoriteMemeDTO> getMyFavoriteMemes(Long userId, int page, int size);
    RandomMemeResponseDTO getHourlyTop10Memes();
}
