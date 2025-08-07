package com.a404.duckonback.controller;

import com.a404.duckonback.dto.AdminArtistPatchDTO;
import com.a404.duckonback.dto.AdminArtistRequestDTO;
import com.a404.duckonback.service.ArtistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Validated
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final ArtistService artistService;

    @PostMapping("/artists")
    public ResponseEntity<Map<String,String>> createArtist(
            @ModelAttribute @Valid AdminArtistRequestDTO dto
    ) {
        artistService.createArtist(dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Map.of("message", "아티스트가 성공적으로 등록되었습니다."));
    }

    @PatchMapping("/artists/{artistId}")
    public ResponseEntity<Map<String,String>> patchArtist(
            @PathVariable Long artistId,
            @ModelAttribute @Valid AdminArtistPatchDTO dto
    ) {
        artistService.patchArtist(artistId, dto);
        return ResponseEntity.ok(Map.of("message", "아티스트 정보가 성공적으로 수정되었습니다."));
    }
}
