package com.a404.duckonback.controller;

import com.a404.duckonback.dto.AdminArtistPatchDTO;
import com.a404.duckonback.dto.AdminArtistRequestDTO;
import com.a404.duckonback.dto.UserRankDTO;
import com.a404.duckonback.dto.UserRankLeaderboardDTO;
import com.a404.duckonback.response.ApiResponseDTO;
import com.a404.duckonback.response.SuccessCode;
import com.a404.duckonback.service.ArtistService;
import com.a404.duckonback.service.EngagementBatchService;
import com.a404.duckonback.service.UserRankService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Validated
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final ArtistService artistService;
    private final UserRankService userRankService;
    private final EngagementBatchService engagementBatchService;

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

    @GetMapping("/users/leaderboard")
    public ResponseEntity<ApiResponseDTO<List<UserRankLeaderboardDTO>>> getUserLeaderboard(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        List<UserRankLeaderboardDTO> leaderboard = userRankService.getUserRankLeaderboard(page, size);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessCode.ADMIN_GET_USER_LEADERBOARD_SUCCESS, leaderboard));
    }

    @PostMapping("/batch/engagement/rebuild")
    public ResponseEntity<ApiResponseDTO> rebuildEngagement() {
        engagementBatchService.rebuildEngagementSnapshot();
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessCode.ADMIN_REBUILD_ENGAGEMENT_SUCCESS));
    }
}
