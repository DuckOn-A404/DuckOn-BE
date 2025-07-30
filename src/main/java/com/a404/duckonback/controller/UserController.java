package com.a404.duckonback.controller;

import com.a404.duckonback.dto.UserInfoResponseDTO;
import com.a404.duckonback.oauth.principal.CustomUserPrincipal;
import com.a404.duckonback.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "사용자 관리", description = "사용자 정보 조회, 팔로우/언팔로우, 회원 탈퇴 등의 기능을 제공합니다.")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "내 정보 조회", description = "로그인한 사용자의 상세 정보를 조회합니다.")
    @GetMapping("/me")
    public ResponseEntity<?> getMyInfo(@AuthenticationPrincipal CustomUserPrincipal principal) {
        return ResponseEntity.ok(userService.getUserDetailInfo(principal.getUser().getUserId()));
    }

    @Operation(summary = "회원 탈퇴", description = "로그인한 사용자의 계정을 삭제합니다.")
    @DeleteMapping("/me")
    public ResponseEntity<?> deleteUser(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestHeader("X-Refresh-Token") String refreshTokenHeader
    ) {

        userService.deleteUser(principal.getUser(), refreshTokenHeader);
        return ResponseEntity.ok(Map.of("message", "회원탈퇴가 완료되었습니다."));
    }

    @Operation(summary = "사용자 정보 조회", description = "특정 사용자의 정보를 조회합니다.")
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

    @Operation(summary = "내 팔로워 조회", description = "로그인한 사용자의 팔로워 목록을 조회합니다.")
    @GetMapping("/me/followers")
    public ResponseEntity<?> getFollowers(@AuthenticationPrincipal CustomUserPrincipal principal) {
        return ResponseEntity.ok(userService.getFollowers(principal.getUser().getUserId()));
    }

    @Operation(summary = "내 팔로잉 조회", description = "로그인한 사용자의 팔로잉 목록을 조회합니다.")
    @GetMapping("/me/following")
    public ResponseEntity<?> getFollowing(@AuthenticationPrincipal CustomUserPrincipal principal) {
        return ResponseEntity.ok(userService.getFollowing(principal.getUser().getUserId()));
    }

    @Operation(summary = "사용자 팔로우", description = "특정 사용자를 팔로우합니다.")
    @PostMapping("/{userId}/follow")
    public ResponseEntity<?> followUser(@AuthenticationPrincipal CustomUserPrincipal principal, @PathVariable String userId) {
        userService.followUser(principal.getUser().getUserId(), userId);
        return ResponseEntity.ok(Map.of("message", "사용자를 팔로우했습니다."));
    }


}