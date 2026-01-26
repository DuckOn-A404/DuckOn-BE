package com.a404.duckonback.domain.artist.emerging.controller;

import com.a404.duckonback.common.filter.CustomUserPrincipal;
import com.a404.duckonback.common.response.ApiResponseDTO;
import com.a404.duckonback.common.response.SuccessCode;
import com.a404.duckonback.domain.artist.emerging.dto.EmergingArtistCreateRequest;
import com.a404.duckonback.domain.artist.emerging.dto.EmergingArtistCreateResponse;
import com.a404.duckonback.domain.artist.emerging.service.EmergingArtistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "라이징 아티스트 관리", description = "라이징 아티스트 정보 조회 등의 기능을 제공합니다.")
@Slf4j
@RestController
@RequestMapping("/api/emerging-artists")
@RequiredArgsConstructor
public class EmergingArtistController {
    private final EmergingArtistService emergingArtistService;

    @Operation(summary = "라이징 아티스트 등록", description = "라이징 아티스트를 등록합니다.")
    @PostMapping
    public ResponseEntity<ApiResponseDTO<EmergingArtistCreateResponse>> createEmergingArtist(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody EmergingArtistCreateRequest emergingArtistCreateRequest
    ){
        EmergingArtistCreateResponse res = emergingArtistService.create(principal.getId(), emergingArtistCreateRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseDTO.created(SuccessCode.CREATE_EMERGING_ARTIST_SUCCESS, res));

    }
}
