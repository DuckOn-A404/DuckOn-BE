package com.a404.duckonback.controller;

import com.a404.duckonback.dto.ArtistDTO;
import com.a404.duckonback.entity.Artist;
import com.a404.duckonback.repository.ArtistFollowRepository;
import com.a404.duckonback.service.ArtistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/artists")
@RequiredArgsConstructor
public class ArtistController {

    private final ArtistService artistService;
    private final ArtistFollowRepository artistFollowRepository;

    // 전체 아티스트 페이징 조회
    @GetMapping
    public ResponseEntity<?> getArtistList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        if (page < 1 || size < 1) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("message", "잘못된 페이지 번호 또는 크기입니다."));
        }

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<ArtistDTO> dtoPage = artistService.getArtists(pageable);

        return ResponseEntity.ok(Map.of(
                "artistList", dtoPage.getContent(),
                "page", page,
                "size", size,
                "totalPages", dtoPage.getTotalPages(),
                "totalElements", dtoPage.getTotalElements()
        ));
    }

    // 키워드 검색
    @GetMapping(params = "keyword")
    public ResponseEntity<?> searchArtists(@RequestParam String keyword) {
        var list = artistService.searchArtists(keyword);
        return ResponseEntity.ok(Map.of("artistList", list));
    }

    // 랜덤 아티스트 조회
    @GetMapping("/random")
    public ResponseEntity<?> getRandomArtists(
            @RequestParam(defaultValue = "16") int size) {
        var list = artistService.getRandomArtists(size);
        return ResponseEntity.ok(Map.of("artistList", list));
    }
}
