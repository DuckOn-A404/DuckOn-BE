package com.a404.duckonback.service;

import com.a404.duckonback.dto.FavoriteMemeResponseDTO;
import com.a404.duckonback.dto.MemeCreateRequestDTO;
import com.a404.duckonback.dto.MemeCreateResponseDTO;
import com.a404.duckonback.dto.RandomMemeResponseDTO;

public interface MemeService {
    MemeCreateResponseDTO createMeme(Long userId, MemeCreateRequestDTO request);
    RandomMemeResponseDTO getRandomMemes(int page, int size);
    void createFavorite(Long userId, Long memeId);
    void deleteFavorite(Long userId, Long memeId);
    FavoriteMemeResponseDTO getMyFavoriteMemes(Long userId, int page, int size);
}
