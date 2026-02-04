package com.a404.duckonback.domain.artist.emerging.controller;

import com.a404.duckonback.common.dto.PageResponse;
import com.a404.duckonback.common.filter.CustomUserPrincipal;
import com.a404.duckonback.common.response.ApiResponseDTO;
import com.a404.duckonback.common.response.SuccessCode;
import com.a404.duckonback.domain.artist.emerging.dto.EmergingArtistCreateRequestDTO;
import com.a404.duckonback.domain.artist.emerging.dto.EmergingArtistCreateResponseDTO;
import com.a404.duckonback.domain.artist.emerging.dto.EmergingArtistDetailResponseDTO;
import com.a404.duckonback.domain.artist.emerging.dto.EmergingArtistListResponseDTO;
import com.a404.duckonback.domain.artist.emerging.service.EmergingArtistFollowService;
import com.a404.duckonback.domain.artist.emerging.service.EmergingArtistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "라이징 아티스트 관리", description = "라이징 아티스트 정보 조회 등의 기능을 제공합니다.")
@Slf4j
@RestController
@RequestMapping("/api/emerging-artists")
@RequiredArgsConstructor
public class EmergingArtistController {
    private final EmergingArtistService emergingArtistService;
    private final EmergingArtistFollowService emergingArtistFollowService;

    @Operation(summary = "라이징 아티스트 등록", description = "라이징 아티스트를 등록합니다.")
    @PostMapping
    public ResponseEntity<ApiResponseDTO<EmergingArtistCreateResponseDTO>> createEmergingArtist(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody EmergingArtistCreateRequestDTO emergingArtistCreateRequestDTO
    ){
        EmergingArtistCreateResponseDTO res = emergingArtistService.create(principal.getId(), emergingArtistCreateRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseDTO.created(SuccessCode.CREATE_EMERGING_ARTIST_SUCCESS, res));
    }

    @Operation(
            summary = "라이징 아티스트 목록/검색/정렬 조회",
            description = "페이지네이션 + 정렬(created/name/debut) + 검색(keyword)을 지원합니다. keyword 검색 결과가 없을시 예외나 null이 아닌 빈 리스트를 반환합니다."
    )
    @GetMapping
    public ResponseEntity<ApiResponseDTO<PageResponse<EmergingArtistListResponseDTO>>> getEmergingArtistList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "followers") String sort,
            @RequestParam(defaultValue = "desc") String order,
            @RequestParam(required = false) String keyword
    ){
        PageResponse<EmergingArtistListResponseDTO> res = emergingArtistService.getList(page, size, sort, order, keyword);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessCode.GET_EMERGING_ARTIST_LIST_SUCCESS, res));
    }

    @Operation(summary = "라이징 아티스트 상세 조회", description = "라이징 아티스트의 상세 정보를 조회합니다. 로그인, 비로그인 사용자 모두 사용할 수 있습니다.")
    @GetMapping("/{emergingArtistId}")
    public ResponseEntity<ApiResponseDTO<EmergingArtistDetailResponseDTO>> getEmergingArtistDetail(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long emergingArtistId
    ){
        EmergingArtistDetailResponseDTO res = emergingArtistService.getEmergingArtistDetail(emergingArtistId, principal != null ? principal.getId() : null);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessCode.GET_EMERGING_ARTIST_DETAIL_SUCCESS, res));
    }

    @Operation(summary = "랜덤 라이징 아티스트 목록 조회", description = "랜덤으로 선택된 라이징 아티스트 목록을 조회합니다.")
    @GetMapping("/random")
    public ResponseEntity<ApiResponseDTO<List<EmergingArtistListResponseDTO>>> getRandomEmergingArtistList(
            @RequestParam(defaultValue = "5") int count
    ){
        List<EmergingArtistListResponseDTO> res = emergingArtistService.getRandomEmergingArtistList(count);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessCode.GET_EMERGING_ARTIST_LIST_SUCCESS, res));
    }

    @Operation(summary = "라이징 아티스트 팔로우", description = "라이징 아티스트를 팔로우합니다.")
    @PostMapping("/{emergingArtistId}/follow")
    public ResponseEntity<ApiResponseDTO<Void>> followEmergingArtist(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long emergingArtistId
    ){
        emergingArtistFollowService.followEmergingArtist(principal.getId(), emergingArtistId);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessCode.FOLLOW_EMERGING_ARTIST_SUCCESS));
    }

    @Operation(summary = "라이징 아티스트 언팔로우", description = "라이징 아티스트를 언팔로우합니다.")
    @DeleteMapping("/{emergingArtistId}/follow")
    public ResponseEntity<ApiResponseDTO<Void>> unfollowEmergingArtist(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long emergingArtistId
    ){
        emergingArtistFollowService.unfollowEmergingArtist(principal.getId(), emergingArtistId);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessCode.UNFOLLOW_EMERGING_ARTIST_SUCCESS));
    }

}
