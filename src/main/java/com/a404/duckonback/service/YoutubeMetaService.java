package com.a404.duckonback.service;


import com.a404.duckonback.dto.YoutubeSearchItemDTO;
import com.a404.duckonback.dto.YoutubeSearchResponseDTO;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class YoutubeMetaService {

    @Value("${youtube.api.key}")
    private String youtubeApiKey;

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

    @Cacheable(cacheNames = "ytSearch", key = "#query + ':' + #maxResults")
    public Mono<YoutubeSearchResponseDTO> search(String query, int maxResults) {
        String url = "https://www.googleapis.com/youtube/v3/search";

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("www.googleapis.com")
                        .path("/youtube/v3/search")
                        .queryParam("part", "snippet")
                        .queryParam("type", "video")
                        .queryParam("maxResults", maxResults)
                        .queryParam("q", query)
                        .queryParam("key", youtubeApiKey)
                        .build())
                .retrieve()
                .bodyToMono(JsonNode.class)
                .doOnNext(body -> log.error("YouTube response body = {}", body))
                .map(this::mapToSearchResponse);
    }

    private YoutubeSearchResponseDTO mapToSearchResponse(JsonNode root) {
        JsonNode itemsNode = root.path("items");
        List<YoutubeSearchItemDTO> items = new ArrayList<>();

        if (itemsNode.isArray()) {
            for (JsonNode itemNode : itemsNode) {
                String videoId = itemNode.path("id").path("videoId").asText("");
                JsonNode snippet = itemNode.path("snippet");

                String title = snippet.path("title").asText("");
                String channelTitle = snippet.path("channelTitle").asText("");

                String thumbnailUrl = snippet.path("thumbnails")
                        .path("medium")
                        .path("url")
                        .asText("");

                if (thumbnailUrl == null || thumbnailUrl.isBlank()) {
                    thumbnailUrl = snippet.path("thumbnails")
                            .path("default")
                            .path("url")
                            .asText("");
                }

                items.add(
                        YoutubeSearchItemDTO.builder()
                                .videoId(videoId)
                                .title(title)
                                .channelTitle(channelTitle)
                                .thumbnailUrl(thumbnailUrl)
                                .build()
                );
            }
        }

        return YoutubeSearchResponseDTO.builder()
                .items(items)
                .build();
    }
}
