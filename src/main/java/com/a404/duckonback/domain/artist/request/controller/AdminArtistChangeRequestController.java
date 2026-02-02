package com.a404.duckonback.domain.artist.request.controller;

import com.a404.duckonback.common.dto.PageResponse;
import com.a404.duckonback.common.response.ApiResponseDTO;
import com.a404.duckonback.common.response.SuccessCode;
import com.a404.duckonback.domain.artist.request.dto.ArtistChangeRequestAdminDetailInfoDTO;
import com.a404.duckonback.domain.artist.request.dto.ArtistChangeRequestAdminInfoDTO;
import com.a404.duckonback.domain.artist.request.service.ArtistChangeRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "관리자 아티스트 정보 변경 요청 관리", description = "관리자가 아티스트 정보 변경 요청을 관리하는 기능을 제공합니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/artist-change-requests")
@PreAuthorize("hasRole('ADMIN')")
public class AdminArtistChangeRequestController {
    private final ArtistChangeRequestService artistChangeRequestService;

    @Operation(
            summary = "모든 아티스트 정보 변경 요청 조회",
            description = "관리자가 모든 사용자의 아티스트 정보 변경 요청 내역을 조회합니다. 관리자 권한이 필요합니다."
    )
    @GetMapping
    public ResponseEntity<ApiResponseDTO<PageResponse<ArtistChangeRequestAdminInfoDTO>>> getAllRequests(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PageResponse<ArtistChangeRequestAdminInfoDTO> response = artistChangeRequestService.getAllRequests(page, size);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessCode.ADMIN_GET_ARTIST_CHANGE_REQUEST_LIST_SUCCESS, response));
    }

    @Operation(
            summary = "아티스트 정보 변경 요청 상세 조회",
            description = "관리자가 특정 아티스트 정보 변경 요청의 상세 내역을 조회합니다. 관리자 권한이 필요합니다."
    )
    @GetMapping("/{requestId}")
    public ResponseEntity<ApiResponseDTO<ArtistChangeRequestAdminDetailInfoDTO>> getDetail(
            @PathVariable Long requestId
    ) {
        ArtistChangeRequestAdminDetailInfoDTO response = artistChangeRequestService.getDetail(requestId);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessCode.ADMIN_GET_ARTIST_CHANGE_REQUEST_DETAIL_SUCCESS, response));
    }

}
