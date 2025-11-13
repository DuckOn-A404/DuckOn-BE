package com.a404.duckonback.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.a404.duckonback.dto.SearchResponseDto;
import com.a404.duckonback.service.SearchService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

@Slf4j
@Tag(name = "검색", description = "검색 기능을 제공합니다.")
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @Operation(summary = "밈 검색", description = "밈을 검색합니다.")
    @GetMapping
    public ResponseEntity<SearchResponseDto> searchMemes(
        @RequestParam("q") String queryTerm,
        @RequestParam(value = "mode", defaultValue = "term") String mode) {

            try {
                log.info("🌐 [API] 밈 검색 요청 - queryTerm: {}, mode: {}", queryTerm, mode);
                SearchResponseDto response = searchService.searchByTag(queryTerm, mode);
                log.info("🌐 [API] 밈 검색 성공 - totalHits: {}, results: {}", response.getTotalHits(), response.getImages().size());
                return ResponseEntity.ok(response);
            } catch (IOException e) {
                log.error("❌ [API] 밈 검색 실패 - queryTerm: {}, mode: {}, error: {}", queryTerm, mode, e.getMessage(), e);
                return ResponseEntity.internalServerError().build();
        }
    }
}

