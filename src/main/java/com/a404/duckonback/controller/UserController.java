package com.a404.duckonback.controller;

import com.a404.duckonback.dto.UserDetailInfoResponseDTO;
import com.a404.duckonback.exception.CustomException;
import com.a404.duckonback.service.UserService;
import com.a404.duckonback.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<?> getMyInfo(@RequestHeader("Authorization") String authorization) {
        try {
            UserDetailInfoResponseDTO userInfo = userService.getUserDetailInfo(authorization);
            return ResponseEntity.ok(userInfo);
        } catch (CustomException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Access Token이 유효하지 않거나 누락되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해주세요."));
        }
    }


    @DeleteMapping("/me")
    public ResponseEntity<?> deleteUser(
            @RequestHeader("Authorization") String accessTokenHeader,
            @RequestHeader("X-Refresh-Token") String refreshTokenHeader) {

        userService.deleteUser(accessTokenHeader, refreshTokenHeader);
        return ResponseEntity.ok(Map.of("message", "회원탈퇴가 완료되었습니다."));
    }


}
