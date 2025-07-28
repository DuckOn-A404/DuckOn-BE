package com.a404.duckonback.controller;

import com.a404.duckonback.dto.UserDetailInfoResponseDTO;
import com.a404.duckonback.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JWTUtil jwtUtil;

    @GetMapping("/me")
    public ResponseEntity<?> getMyInfo(@RequestHeader("Authorization") String authorization) {
        UserDetailInfoResponseDTO userInfo = userService.getUserDetailInfo(authorization);
        return ResponseEntity.ok(userInfo);
    }


    @DeleteMapping("/me")
    public ResponseEntity<?> deleteUser(
            @RequestHeader("Authorization") String accessTokenHeader,
            @RequestHeader("X-Refresh-Token") String refreshTokenHeader) {

        userService.deleteUser(accessTokenHeader, refreshTokenHeader);
        return ResponseEntity.ok(Map.of("message", "회원탈퇴가 완료되었습니다."));
    }


}
