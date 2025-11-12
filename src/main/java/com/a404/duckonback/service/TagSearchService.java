package com.a404.duckonback.service;

import com.a404.duckonback.dto.TrendingTagDTO;

import java.time.Duration;
import java.util.List;

public interface TagSearchService {
    void logSearchKeyword(String keyword);
    List<TrendingTagDTO> getTrendingTags(Duration range, int size);
}
