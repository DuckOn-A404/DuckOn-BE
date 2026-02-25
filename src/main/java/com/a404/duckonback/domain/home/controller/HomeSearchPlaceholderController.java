package com.a404.duckonback.domain.home.controller;

import com.a404.duckonback.common.response.ApiResponseDTO;
import com.a404.duckonback.common.response.SuccessCode;
import com.a404.duckonback.common.util.HttpCacheUtil;
import com.a404.duckonback.domain.home.dto.HomeSearchPlaceholderResponseDTO;
import com.a404.duckonback.domain.home.service.HomeSearchPlaceholderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "홈 검색창 플레이스홀더", description = "홈 검색창 플레이스홀더 관련 API")
@RestController
@RequestMapping("/api/home/search-placeholder")
@RequiredArgsConstructor
@Validated
public class HomeSearchPlaceholderController {

    private static final long CACHE_MAX_AGE_SECONDS = 3600; // 1시간
    private static final String ETAG_PREFIX = "home-search-placeholder";

    private final HomeSearchPlaceholderService homeSearchPlaceholderService;

    @Operation(summary = "홈 검색창 플레이스홀더 조회", description = "홈 검색창에 표시할 플레이스홀더 문구를 조회합니다. ETag 기반 캐싱 지원. JWT 필요 X.")
    @GetMapping
    public ResponseEntity<?> getSearchPlaceholder(
            @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch
    ) {
        HomeSearchPlaceholderResponseDTO response = homeSearchPlaceholderService.getPlaceholders();

        String etag = HttpCacheUtil.weakEtag(ETAG_PREFIX, response.getVersion());

        if(HttpCacheUtil.isNotModified(ifNoneMatch, etag)){
            return HttpCacheUtil.notModified(etag, CACHE_MAX_AGE_SECONDS);
        }

        return HttpCacheUtil.ok(
                ApiResponseDTO.success(SuccessCode.GET_HOME_SEARCH_PLACEHOLDER_SUCCESS, response),
                etag,
                CACHE_MAX_AGE_SECONDS
        );
    }
}
