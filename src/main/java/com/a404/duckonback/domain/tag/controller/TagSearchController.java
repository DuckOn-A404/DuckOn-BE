package com.a404.duckonback.domain.tag.controller;

import com.a404.duckonback.common.response.ApiResponseDTO;
import com.a404.duckonback.common.response.SuccessCode;
import com.a404.duckonback.domain.tag.service.TagSearchService;
import com.a404.duckonback.domain.tag.dto.SearchTagLogRequestDTO;
import com.a404.duckonback.domain.tag.dto.TagSearchResponseDTO;
import com.a404.duckonback.domain.tag.dto.TrendingTagDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Tag(name = "태그 검색 로그 및 인기 태그", description = "태그 검색 로그 기록 및 실시간 인기 태그 조회 기능을 제공합니다.")
@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagSearchController {
    private final TagSearchService tagSearchService;

    @Operation(summary = "태그 검색 키워드 로그 기록", description = "사용자가 검색한 태그 키워드를 로그로 기록합니다.")
    @PostMapping("/search/log")
    public ResponseEntity<ApiResponseDTO<Void>> logSearchKeyword(@RequestBody SearchTagLogRequestDTO request) {
        tagSearchService.logSearchKeyword(request.getKeyword());
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessCode.TAG_SEARCH_LOG_SUCCESS));
    }

    @Operation(summary = "태그 자동완성 검색 (JWT 필요X)", description = "입력된 키워드로 태그를 검색합니다. 접두어 일치 결과를 우선으로, 부족하면 부분 일치 결과를 추가합니다.")
    @GetMapping("/search")
    public ResponseEntity<ApiResponseDTO<List<TagSearchResponseDTO>>> searchTags(
            @RequestParam(name = "q") String keyword,
            @RequestParam(defaultValue = "10") int limit
    ) {
        List<TagSearchResponseDTO> results = tagSearchService.searchTags(keyword, limit)
                .stream()
                .map(TagSearchResponseDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessCode.TAG_SEARCH_SUCCESS, results));
    }

    // 실시간 인기 태그 조회
    @Operation(summary = "실시간 인기 태그 조회 (JWT 필요X)", description = "지정된 기간 동안 가장 많이 검색된 인기 태그를 조회합니다. 기간은 10분, 1시간, 1일 중 선택할 수 있습니다.")
    @GetMapping("/trending")
    public ResponseEntity<ApiResponseDTO<List<TrendingTagDTO>>> getTrendingTags(
            @RequestParam(defaultValue = "HOUR") String range,
            @RequestParam(defaultValue = "10") int size
    ) {
        Duration duration = switch (range.toUpperCase()) {
            case "10M" -> Duration.ofMinutes(10);
            case "DAY" -> Duration.ofDays(1);
            case "HOUR" -> Duration.ofHours(1);
            default -> Duration.ofHours(1);
        };

        List<TrendingTagDTO> trending = tagSearchService.getTrendingTags(duration, size);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessCode.TAG_TRENDING_RETRIEVE_SUCCESS, trending));
    }

    // 실시간 인기 태그 조회
    @Operation(summary = "기본 인기 태그 조회(10개) (JWT 필요X)", description = "지정된 기간 동안 가장 많이 검색된 인기 태그를 조회합니다. 기간은 10분, 1시간, 1일 중 선택할 수 있습니다.")
    @GetMapping("/basic")
    public ResponseEntity<ApiResponseDTO<List<String>>> getTagsBasic(
    ) {
        String[] tags = new String[] {"Aespa", "blackping", "jennie", "karina", "kiss of life", "winter", "블랙핑크", "장원영","안유진","카리나", "rose","아이즈원"};
        List<String> list = new ArrayList<>(Arrays.asList(tags));
        Collections.shuffle(list);              // 리스트 전체 섞기

        List<String> result = list.subList(0, 10);


        return ResponseEntity.ok(ApiResponseDTO.success(SuccessCode.TAG_TRENDING_RETRIEVE_SUCCESS, result));
    }
}
