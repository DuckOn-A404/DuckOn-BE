package com.a404.duckonback.controller;

import com.a404.duckonback.dto.UserInfoResponseDTO;
import com.a404.duckonback.filter.CustomUserDetails;
import com.a404.duckonback.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<?> getMyInfo(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(userService.getUserDetailInfo(userDetails.getUser().getUserId()));
    }


    @DeleteMapping("/me")
    public ResponseEntity<?> deleteUser(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestHeader("X-Refresh-Token") String refreshTokenHeader
    ) {

        userService.deleteUser(userDetails.getUser(), refreshTokenHeader);
        return ResponseEntity.ok(Map.of("message", "회원탈퇴가 완료되었습니다."));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String userId
    ) {
        UserInfoResponseDTO userInfo = userService.getUserInfo(userDetails.getUser().getUserId(), userId);
        return ResponseEntity.ok(userInfo);
    }

    @GetMapping("/me/followers")
    public ResponseEntity<?> getFollowers(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(userService.getFollowers(userDetails.getUser().getUserId()));
    }

    @GetMapping("/me/following")
    public ResponseEntity<?> getFollowing(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(userService.getFollowing(userDetails.getUser().getUserId()));
    }


}
