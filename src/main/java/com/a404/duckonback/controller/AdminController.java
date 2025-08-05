package com.a404.duckonback.controller;

import com.a404.duckonback.dto.AdminArtistRequestDTO;
import com.a404.duckonback.dto.AdminArtistResponseDTO;
import com.a404.duckonback.service.ArtistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Validated
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final ArtistService artistService;

    @PostMapping("/artists")
    public ResponseEntity<AdminArtistResponseDTO> createArtist(
            @ModelAttribute @Valid AdminArtistRequestDTO dto
    ) {
        artistService.createArtist(dto);
        AdminArtistResponseDTO res = AdminArtistResponseDTO.builder()
                .message("아티스트가 성공적으로 등록되었습니다.")
                .build();
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(res);
    }
}
