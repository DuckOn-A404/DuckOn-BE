package com.a404.duckonback.controller;

import com.a404.duckonback.dto.UserDetailInfoResponseDTO;
import com.a404.duckonback.dto.UserInfoResponseDTO;
import com.a404.duckonback.service.UserService;
import com.a404.duckonback.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JWTUtil jwtUtil;

    @GetMapping("/me")
    public ResponseEntity<?> getUserDetailInfo(@RequestHeader("Authorization") String authorization) {
        UserDetailInfoResponseDTO userDetailInfo = userService.getUserDetailInfo(authorization);
        return ResponseEntity.ok(userDetailInfo);
    }

    @DeleteMapping("/me")
    public ResponseEntity<?> deleteUser(
            @RequestHeader("Authorization") String accessTokenHeader,
            @RequestHeader("X-Refresh-Token") String refreshTokenHeader)
    {
        userService.deleteUser(accessTokenHeader, refreshTokenHeader);
        return ResponseEntity.ok(Map.of("message", "회원탈퇴가 완료되었습니다."));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserInfo(@PathVariable String userId) {
        UserInfoResponseDTO userInfo = userService.getUserInfo(userId);
        return ResponseEntity.ok(userInfo);
    }


}
