package com.a404.duckonback.controller;

import com.a404.duckonback.dto.ArtistDTO;
import com.a404.duckonback.dto.ArtistDetailDTO;
import com.a404.duckonback.dto.FollowedArtistDTO;
import com.a404.duckonback.dto.UpdateArtistFollowRequestDTO;
import com.a404.duckonback.filter.CustomUserDetailsService;
import com.a404.duckonback.service.ArtistFollowService;
import com.a404.duckonback.service.ArtistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "아티스트 관리", description = "아티스트 정보 조회, 팔로우/언팔로우, 검색 등의 기능을 제공합니다.")
@Slf4j
@RestController
@RequestMapping("/api/artists")
@RequiredArgsConstructor
public class ArtistController {

    private final ArtistService artistService;
    private final ArtistFollowService artistFollowService;

    // 단일 아티스트 상세 조회
    @Operation(summary = "아티스트 상세 조회",
            description = "특정 아티스트의 상세 정보를 조회합니다. 로그인한 사용자의 팔로우 상태도 함께 반환됩니다.")
    @GetMapping("/{artistId}")
    public ResponseEntity<?> getArtist(
            @PathVariable Long artistId,
            @AuthenticationPrincipal CustomUserDetailsService.CustomUserPrincipal principal
    ) {
        Long userId = principal != null
                ? principal.getUser().getId()
                : null;
        ArtistDetailDTO dto = artistService.getArtistDetail(userId, artistId);
        return ResponseEntity.ok(dto);
    }

    // 전체 아티스트 페이징 조회
    @Operation(summary = "아티스트 목록 조회",
            description = "전체 아티스트 목록을 페이지 단위로 조회합니다. 페이지 번호와 크기를 지정할 수 있습니다.")
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
    @Operation(summary = "아티스트 검색",
            description = "아티스트 이름이나 설명을 키워드로 검색합니다. 검색 결과는 페이지 단위로 반환됩니다.")
    @GetMapping(params = "keyword")
    public ResponseEntity<?> searchArtists(@RequestParam String keyword) {
        var list = artistService.searchArtists(keyword);
        return ResponseEntity.ok(Map.of("artistList", list));
    }

    // 랜덤 아티스트 조회
    @Operation(summary = "랜덤 아티스트 조회",
            description = "지정된 크기만큼 랜덤으로 아티스트를 조회합니다. 기본 크기는 16입니다.")
    @GetMapping("/random")
    public ResponseEntity<?> getRandomArtists(
            @RequestParam(defaultValue = "16") int size) {
        var list = artistService.getRandomArtists(size);
        return ResponseEntity.ok(Map.of("artistList", list));
    }

    // 내가 팔로우한 아티스트 조회
    @Operation(summary = "내가 팔로우한 아티스트 조회",
            description = "로그인한 사용자가 팔로우한 아티스트 목록을 페이지 단위로 조회합니다.")
    @GetMapping("/me")
    public ResponseEntity<?> getMyFollowedArtists(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal CustomUserDetailsService.CustomUserPrincipal principal) {

        if (page < 1 || size < 1) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("message", "잘못된 페이지 번호 또는 크기입니다."));
        }

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<FollowedArtistDTO> dtoPage = artistFollowService.getFollowedArtists(
                principal.getUser().getId(), pageable);

        return ResponseEntity.ok(Map.of(
                "artistList", dtoPage.getContent(),
                "page", page,
                "size", size,
                "totalPages", dtoPage.getTotalPages(),
                "totalElements", dtoPage.getTotalElements()
        ));
    }

    // 아티스트 팔로우 추가
    @Operation(summary = "아티스트 팔로우",
            description = "로그인한 사용자가 특정 아티스트를 팔로우합니다. 성공 시 201 상태 코드를 반환합니다.")
    @PostMapping("/{artistId}/follow")
    public ResponseEntity<?> followArtist(
            @PathVariable Long artistId,
            @AuthenticationPrincipal CustomUserDetailsService.CustomUserPrincipal principal
    ) {
        Long userId = principal.getUser().getId();
        artistFollowService.followArtist(userId, artistId);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Map.of("message", "아티스트를 팔로우했습니다."));
    }

    // 아티스트 팔로우 취소
    @Operation(summary = "아티스트 팔로우 취소",
            description = "로그인한 사용자가 특정 아티스트의 팔로우를 취소합니다. 성공 시 200 상태 코드를 반환합니다.")
    @DeleteMapping("/{artistId}/follow")
    public ResponseEntity<?> unfollowArtist(
            @PathVariable Long artistId,
            @AuthenticationPrincipal CustomUserDetailsService.CustomUserPrincipal principal
    ) {
        Long userId = principal.getUser().getId();
        artistFollowService.unfollowArtist(userId, artistId);
        return ResponseEntity
                .ok(Map.of("message", "아티스트 팔로우를 취소했습니다."));
    }

    // 아티스트 팔로우 수정
    @Operation(summary = "아티스트 팔로우 목록 수정",
            description = "로그인한 사용자가 팔로우할 아티스트 목록을 수정합니다. 기존 팔로우는 모두 취소되고, 요청된 아티스트만 팔로우됩니다.")
    @PutMapping("/follow")
    public ResponseEntity<?> updateFollows(
            @RequestBody UpdateArtistFollowRequestDTO req,
            @AuthenticationPrincipal CustomUserDetailsService.CustomUserPrincipal principal
    ) {
        Long userId = principal.getUser().getId();
        artistFollowService.updateArtistFollows(userId, req.getArtistList());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Map.of("message", "아티스트 팔로우 목록을 수정했습니다."));
    }

}
