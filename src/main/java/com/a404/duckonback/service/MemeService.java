package com.a404.duckonback.service;

import com.a404.duckonback.dto.*;

import java.util.List;

public interface MemeService {
    MemeCreateResponseDTO createMemes(Long userId, MemeCreateRequestDTO request);
    void deleteMeme(Long userId, Long memeId);
    MemeResponseDTO getRandomMemes(int page, int size);
    void createFavorite(Long userId, Long memeId);
    void deleteFavorite(Long userId, Long memeId);
    List<FavoriteMemeDTO> getMyFavoriteMemes(Long userId, int page, int size);
    MemeResponseDTO getHourlyTop10Memes();
    MemeResponseDTO getTop10MemesByTotalUsage();
    List<MyMemeDTO> getMyMemes(Long userId, int page, int size);

    MemeDetailDTO getMemeDetail(Long memeId);
    MemeResponseDTO searchByTagBasic(String tag, int page, int size);
}
