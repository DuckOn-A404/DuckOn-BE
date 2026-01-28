package com.a404.duckonback.service;

import com.a404.duckonback.dto.*;


public interface MemeService {
    MemeCreateResponseDTO createMemes(Long userId, MemeCreateRequestDTO request);
    void deleteMeme(Long userId, Long memeId);
    MemeDetailDTO updateMeme(Long userId, Long memeId, MemeUpdateRequestDTO request);
    MemeResponseDTO getRandomMemes(int page, int size);
    void createFavorite(Long userId, Long memeId);
    void deleteFavorite(Long userId, Long memeId);
    MemeResponseDTO getMyFavoriteMemes(Long userId, int page, int size);
    MemeResponseDTO getHourlyTop10Memes();
    MemeResponseDTO getTop10MemesByTotalUsage();

    MemeDetailDTO getMemeDetail(Long memeId);
    MemeResponseDTO searchByTagBasic(String tag, int page, int size);
}
