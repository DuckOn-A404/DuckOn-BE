package com.a404.duckonback.controller;

import com.a404.duckonback.service.YoutubeMetaService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/public/youtube")
@RequiredArgsConstructor
public class YoutubeMetaController {
    private final YoutubeMetaService svc;

    @Operation(summary = "유튜브 메타 데이터 (JWT 필요X)")
    @GetMapping("/meta/{videoId}")
    public Mono<YoutubeMetaService.YoutubeMeta> meta(@PathVariable String videoId) {
        return svc.getMeta(videoId);
    }
}
