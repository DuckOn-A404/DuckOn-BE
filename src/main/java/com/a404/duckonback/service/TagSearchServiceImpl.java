package com.a404.duckonback.service;

import com.a404.duckonback.dto.TrendingTagDTO;
import com.a404.duckonback.entity.Tag;
import com.a404.duckonback.entity.TagSearchLog;
import com.a404.duckonback.repository.TagRepository;
import com.a404.duckonback.repository.TagSearchLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class TagSearchServiceImpl implements TagSearchService{

    private final TagRepository tagRepository;
    private final TagSearchLogRepository tagSearchLogRepository;

    @Override
    @Transactional
    public void logSearchKeyword(String keyword) {
        String normalizedKeyword = normalizeKeyword(keyword);
        if(normalizedKeyword.isBlank()) {
            return;
        }

        Tag tag = tagRepository.findByTagName(normalizedKeyword)
                .orElseGet(() -> tagRepository.save(
                        Tag.builder().tagName(normalizedKeyword).build()
                ));

        TagSearchLog tagSearchLog = TagSearchLog.builder()
                .tag(tag)
                .keywordRaw(keyword)
                .build();

        tagSearchLogRepository.save(tagSearchLog);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrendingTagDTO> getTrendingTags(Duration range, int size) {
        LocalDateTime from = LocalDateTime.now().minus(range);
        Pageable pageable = PageRequest.of(0, size);
        return tagSearchLogRepository.findTrendingTags(from, pageable);
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null) return "";
        // 예시 정책: trim + 소문자화
        return keyword.trim().toLowerCase(Locale.ROOT);
        // 한글 대소문자 구분 없으면 그대로 두고, 필요시 공백/특수문자 처리 추가
    }
}
