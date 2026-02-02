package com.a404.duckonback.domain.artist.request.controller;

import com.a404.duckonback.common.filter.CustomUserPrincipal;
import com.a404.duckonback.common.response.ApiResponseDTO;
import com.a404.duckonback.common.response.SuccessCode;
import com.a404.duckonback.domain.artist.request.dto.ArtistChangeRequestCreateRequestDTO;
import com.a404.duckonback.domain.artist.request.service.ArtistChangeRequestService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/artist-change-requests")
public class ArtistChangeRequestController {

    private final ArtistChangeRequestService artistChangeRequestService;

    @Operation(
            summary = "아티스트 정보 변경 요청 생성",
            description = "사용자가 아티스트 정보 변경을 요청합니다. JWT 인증이 필요합니다."
    )
    @PostMapping
    public ResponseEntity<ApiResponseDTO<Void>> create(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody ArtistChangeRequestCreateRequestDTO request
    ){
        artistChangeRequestService.create(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseDTO.created(SuccessCode.CREATE_ARTIST_CHANGE_REQUEST_SUCCESS));
    }
}
