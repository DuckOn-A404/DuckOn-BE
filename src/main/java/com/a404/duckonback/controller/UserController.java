package com.a404.duckonback.controller;

import com.a404.duckonback.dto.RecommendUsersResponseDTO;
import com.a404.duckonback.dto.UpdateProfileRequestDTO;
import com.a404.duckonback.dto.UserInfoResponseDTO;
import com.a404.duckonback.dto.UserRankLeaderboardDTO;
import com.a404.duckonback.filter.CustomUserPrincipal;
import com.a404.duckonback.response.ApiResponseDTO;
import com.a404.duckonback.response.SuccessCode;
import com.a404.duckonback.service.UserRankService;
import com.a404.duckonback.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "사용자 관리", description = "다른 사용자 정보 조회, 추천, 유저보드 등")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRankService userRankService;

    @Operation(summary = "사용자 정보 조회 (JWT 필요O)", description = "특정 사용자의 정보를 조회합니다.")
    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserInfo(
            @Parameter(
                    description = "조회할 사용자의 ID",
                    required = true,
                    example = "user5@example.com"
            )
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable String userId
    ) {
        UserInfoResponseDTO userInfo = userService.getUserInfo(principal.getUser().getUserId(), userId);
        return ResponseEntity.ok(userInfo);
    }

    @Operation(summary = "사용자 추천 (JWT 필요X)", description = "로그인/비로그인 모두 사용 가능. 현재 사용자에게 어울리는 다른 사용자를 추천합니다.")
    @GetMapping("/recommendations")
    public ResponseEntity<?> recommendUsers(
            @AuthenticationPrincipal CustomUserPrincipal principal, // null일 수 있음
            @RequestParam(required = false) Long artistId,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "true") boolean includeReasons
    ) {
        String myUserId = (principal != null && principal.getUser() != null)
                ? principal.getUser().getUserId()
                : null; // 게스트면 null

        RecommendUsersResponseDTO res = userService.recommendUsers(myUserId, artistId, size, includeReasons);
        return ResponseEntity.ok(res);
    }

    @Operation(summary = "유저 리더보드 조회 (JWT 필요X)", description = "유저 참여도 지표 기반 리더보드를 조회합니다.")
    @GetMapping("/leaderboard")
    public ResponseEntity<ApiResponseDTO<List<UserRankLeaderboardDTO>>> getUserLeaderboard(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        List<UserRankLeaderboardDTO> leaderboard = userRankService.getUserRankLeaderboard(page, size);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessCode.GET_USER_LEADERBOARD_SUCCESS, leaderboard));
    }

}