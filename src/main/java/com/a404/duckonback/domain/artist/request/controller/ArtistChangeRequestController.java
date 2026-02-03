package com.a404.duckonback.domain.artist.request.controller;

import com.a404.duckonback.common.dto.PageResponse;
import com.a404.duckonback.common.filter.CustomUserPrincipal;
import com.a404.duckonback.common.response.ApiResponseDTO;
import com.a404.duckonback.common.response.SuccessCode;
import com.a404.duckonback.domain.artist.request.dto.ArtistChangeRequestCreateRequestDTO;
import com.a404.duckonback.domain.artist.request.dto.ArtistChangeRequestInfoDTO;
import com.a404.duckonback.domain.artist.request.service.ArtistChangeRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "아티스트 정보 변경 요청 관리", description = "사용자가 아티스트 정보 변경을 요청하고 자신의 요청 내역을 조회하는 기능을 제공합니다.")
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

    @Operation(
            summary = "내 아티스트 정보 변경 요청 조회",
            description = "사용자가 자신의 아티스트 정보 변경 요청 내역을 조회합니다. JWT 인증이 필요합니다."
    )
    @GetMapping("/me")
    public ResponseEntity<ApiResponseDTO<PageResponse<ArtistChangeRequestInfoDTO>>> getMyRequests(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        PageResponse<ArtistChangeRequestInfoDTO> response = artistChangeRequestService.getMyRequests(page, size, principal.getId());
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessCode.GET_MY_ARTIST_CHANGE_REQUEST_LIST_SUCCESS, response));
    }
}
