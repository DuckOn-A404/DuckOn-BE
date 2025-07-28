package com.a404.duckonback.controller;

import com.a404.duckonback.dto.ArtistDTO;
import com.a404.duckonback.entity.Artist;
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

    @GetMapping
    public ResponseEntity<?> getArtistList(@RequestParam(defaultValue = "1") int page,
                                           @RequestParam(defaultValue = "10") int size) {
        if (page < 1 || size < 1) {
            return ResponseEntity.badRequest().body(Map.of("message", "잘못된 페이지 번호 또는 크기입니다."));
        }

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Artist> artistPage = artistService.getArtists(pageable);

        List<ArtistDTO> artistList = artistPage.getContent().stream()
                .map(ArtistDTO::fromEntity)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("artistList", artistList);
        response.put("page", page);
        response.put("size", size);
        response.put("totalPages", artistPage.getTotalPages());
        response.put("totalElements", artistPage.getTotalElements());

        return ResponseEntity.ok(response);
    }

    @GetMapping(params = "keyword")
    public ResponseEntity<?> searchArtists(@RequestParam String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "keyword는 필수 파라미터입니다."));
        }

        log.info("Searching artists with keyword: {}", keyword);

        List<Artist> artistList = artistService.searchArtists(keyword);
        List<ArtistDTO> responseList = artistList.stream()
                .map(ArtistDTO::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of("artistList", responseList));
    }


}