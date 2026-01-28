package com.a404.duckonback.domain.tag.service;

import com.a404.duckonback.domain.tag.dto.TrendingTagDTO;
import com.a404.duckonback.domain.tag.entity.Tag;

import java.time.Duration;
import java.util.List;

public interface TagSearchService {
    void logSearchKeyword(String keyword);
    List<TrendingTagDTO> getTrendingTags(Duration range, int size);
    List<Tag> searchTags(String keyword, int limit);
}
