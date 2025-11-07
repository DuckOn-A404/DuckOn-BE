package com.a404.duckonback.service;

import com.a404.duckonback.dto.MemeCreateRequestDTO;

public interface MemeService {
    void createMeme(Long userId, MemeCreateRequestDTO request);
}
