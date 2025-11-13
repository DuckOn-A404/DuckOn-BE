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

@Tag(name = "사용자 관리", description = "사용자 정보 조회, 팔로우/언팔로우, 회원 탈퇴 등의 기능을 제공합니다.")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRankService userRankService;

    @Operation(summary = "내 정보 조회 (JWT 필요O)", description = "로그인한 사용자의 상세 정보를 조회합니다.")
    @GetMapping("/me")
    public ResponseEntity<?> getMyInfo(@AuthenticationPrincipal CustomUserPrincipal principal) {
        return ResponseEntity.ok(userService.getUserDetailInfo(principal.getUser().getUserId()));
    }

    @Operation(summary = "내 정보 수정 (JWT 필요O)", description = "로그인한 사용자의 정보를 수정합니다.")
    @PatchMapping(
            value = "/me",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> updateMyInfo(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @ModelAttribute UpdateProfileRequestDTO newUserInfo
    ) {
        userService.updateUserInfo(principal.getUser().getUserId(), newUserInfo);
        return ResponseEntity.ok(Map.of("message", "회원 정보가 수정되었습니다."));
    }

    @Operation(summary = "회원 탈퇴 (JWT 필요O)", description = "로그인한 사용자의 계정을 삭제합니다.")
    @DeleteMapping("/me")
    public ResponseEntity<?> deleteUser(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestHeader("X-Refresh-Token") String refreshTokenHeader
    ) {

        userService.deleteUser(principal.getUser(), refreshTokenHeader);
        return ResponseEntity.ok(Map.of("message", "회원탈퇴가 완료되었습니다."));
    }

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

    @Operation(summary = "내 팔로워 조회 (JWT 필요O)", description = "로그인한 사용자의 팔로워 목록을 조회합니다.")
    @GetMapping("/me/followers")
    public ResponseEntity<?> getFollowers(@AuthenticationPrincipal CustomUserPrincipal principal) {
        return ResponseEntity.ok(userService.getFollowers(principal.getUser().getUserId()));
    }

    @Operation(summary = "내 팔로잉 조회 (JWT 필요O)", description = "로그인한 사용자의 팔로잉 목록을 조회합니다.")
    @GetMapping("/me/following")
    public ResponseEntity<?> getFollowing(@AuthenticationPrincipal CustomUserPrincipal principal) {
        return ResponseEntity.ok(userService.getFollowing(principal.getUser().getUserId()));
    }

    @Operation(summary = "사용자 팔로우 (JWT 필요O)", description = "특정 사용자를 팔로우합니다.")
    @PostMapping("/{userId}/follow")
    public ResponseEntity<?> followUser(@AuthenticationPrincipal CustomUserPrincipal principal, @PathVariable String userId) {
        userService.followUser(principal.getUser().getUserId(), userId);
        return ResponseEntity.ok(Map.of("message", "사용자를 팔로우했습니다."));
    }

    @Operation(summary = "사용자 언팔로우 (JWT 필요O)", description = "특정 사용자의 팔로우를 취소합니다.")
    @DeleteMapping("/{userId}/follow")
    public ResponseEntity<?> unfollowUser(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable String userId
    ) {
        userService.unfollowUser(principal.getUser().getUserId(), userId);
        return ResponseEntity.ok(Map.of("message", "사용자 팔로우를 취소했습니다."));
    }

    @Operation(summary = "비밀번호 확인 (JWT 필요O)", description = "입력한 비밀번호가 현재 사용자의 비밀번호와 일치하는지 확인합니다.")
    @PostMapping("/me/verify-password")
    public ResponseEntity<?> verifyPassword(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestBody Map<String, String> request
    ) {
        String inputPassword = request.get("password");
        boolean isValid = userService.verifyPassword(principal.getUser().getUserId(), inputPassword);
        return ResponseEntity.ok(Map.of("valid", isValid));
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