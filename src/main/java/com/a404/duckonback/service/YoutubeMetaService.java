package com.a404.duckonback.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class YoutubeMetaService {
    private final WebClient webClient = WebClient.builder()
            .defaultHeader(HttpHeaders.USER_AGENT, "DuckOnMeta/1.0")
            .build();

    @Cacheable(cacheNames = "ytMeta", key = "#videoId")
    public Mono<YoutubeMeta> getMeta(String videoId) {
        if (!videoId.matches("^[A-Za-z0-9_-]{11}$")) {
            return Mono.just(new YoutubeMeta("", "", thumb(videoId)));
        }
        String yto = "https://www.youtube.com/oembed?format=json&url=https://www.youtube.com/watch?v=" + videoId;
        String neb = "https://noembed.com/embed?url=https://www.youtube.com/watch?v=" + videoId;

        return fetch(yto)
                .onErrorResume(e -> fetch(neb))
                .defaultIfEmpty(new YoutubeMeta("", "", thumb(videoId)))
                .timeout(Duration.ofSeconds(2)) // 느릴 때 UX 보호
                .onErrorReturn(new YoutubeMeta("", "", thumb(videoId)));
    }

    private Mono<YoutubeMeta> fetch(String url) {
        return webClient.get().uri(url)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(j -> new YoutubeMeta(
                        text(j, "title"),
                        text(j, "author_name", "author"),
                        j.has("thumbnail_url") ? j.get("thumbnail_url").asText() : thumbFromUrl(url)
                ));
    }

    private String text(JsonNode j, String... keys) {
        for (String k : keys) {
            if (j.has(k)) return j.get(k).asText("");
        }
        return "";
    }
    private String thumb(String id) { return "https://img.youtube.com/vi/" + id + "/hqdefault.jpg"; }
    private String thumbFromUrl(String url) {
        String id = url.substring(url.lastIndexOf('=') + 1);
        return thumb(id);
    }

    public record YoutubeMeta(String title, String author, String thumbnail) {}
}
