package com.a404.duckonback.controller;

import com.a404.duckonback.dto.YoutubeSearchResponseDTO;
import com.a404.duckonback.service.YoutubeMetaService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/public/youtube")
@RequiredArgsConstructor
public class YoutubeMetaController {
    private final YoutubeMetaService youtueMetaService;

    @Operation(summary = "유튜브 메타 데이터 (JWT 필요X)")
    @GetMapping("/meta/{videoId}")
    public Mono<YoutubeMetaService.YoutubeMeta> meta(@PathVariable String videoId) {
        return youtueMetaService.getMeta(videoId);
    }

    @Operation(summary = "유튜브 영상 검색 (JWT 필요X)")
    @GetMapping("/search")
    public Mono<YoutubeSearchResponseDTO> search(
            @RequestParam("query") String query,
            @RequestParam(name = "maxResults", defaultValue = "12") int maxResults
    ) {
        return youtueMetaService.search(query, maxResults);
    }
}
