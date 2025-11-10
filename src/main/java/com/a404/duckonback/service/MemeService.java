package com.a404.duckonback.service;

import com.a404.duckonback.dto.MemeCreateRequestDTO;
import com.a404.duckonback.dto.MemeCreateResponseDTO;

public interface MemeService {
    MemeCreateResponseDTO createMeme(Long userId, MemeCreateRequestDTO request);
    void createFavorite(Long userId, Long memeId);
    void deleteFavorite(Long userId, Long memeId);
}
