package com.a404.duckonback.controller;

import com.a404.duckonback.dto.UserInfoResponseDTO;
import com.a404.duckonback.oauth.principal.CustomUserPrincipal;
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
    public ResponseEntity<?> getMyInfo(@AuthenticationPrincipal CustomUserPrincipal principal) {
        return ResponseEntity.ok(userService.getUserDetailInfo(principal.getUser().getUserId()));
    }


    @DeleteMapping("/me")
    public ResponseEntity<?> deleteUser(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestHeader("X-Refresh-Token") String refreshTokenHeader
    ) {

        userService.deleteUser(principal.getUser(), refreshTokenHeader);
        return ResponseEntity.ok(Map.of("message", "회원탈퇴가 완료되었습니다."));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserInfo(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable String userId
    ) {
        UserInfoResponseDTO userInfo = userService.getUserInfo(principal.getUser().getUserId(), userId);
        return ResponseEntity.ok(userInfo);
    }


}
