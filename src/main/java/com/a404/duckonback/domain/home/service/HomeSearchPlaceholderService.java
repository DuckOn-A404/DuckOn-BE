package com.a404.duckonback.domain.home.service;

import com.a404.duckonback.domain.home.dto.HomeSearchPlaceholderResponseDTO;

import java.util.List;

public interface HomeSearchPlaceholderService {
    HomeSearchPlaceholderResponseDTO getPlaceholders();
    HomeSearchPlaceholderResponseDTO updatePlaceholders(List<String> items, Long adminUserId);
}
