package com.a404.duckonback.controller;

import com.a404.duckonback.dto.LoginRequestDTO;
import com.a404.duckonback.dto.SignupRequestDTO;
import com.a404.duckonback.exception.CustomException;
import com.a404.duckonback.filter.CustomUserDetails;
import com.a404.duckonback.service.AuthService;
import com.a404.duckonback.service.AuthServiceImpl;
import com.a404.duckonback.service.UserService;
import com.a404.duckonback.service.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO loginRequest){
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    @PostMapping(value = "/signup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> signup(@ModelAttribute SignupRequestDTO request) {


        try {
            return authService.signup(request);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 내부 오류가 발생했습니다.");
        }
    }


    @GetMapping("/email/exists")
    public ResponseEntity<?> checkEmailDuplicate(@RequestParam String email) {
        boolean isDuplicate = userService.isEmailDuplicate(email);
        return ResponseEntity.ok().body(Map.of("isDuplicate", isDuplicate));
    }

    @GetMapping("/user-id/exists")
    public ResponseEntity<?> checkUserIdDuplicate(@RequestParam String userId) {
        boolean isDuplicate = userService.isUserIdDuplicate(userId);
        return ResponseEntity.ok().body(Map.of("isDuplicate", isDuplicate));
    }

    @GetMapping("/nickname/exists")
    public ResponseEntity<?> checkNicknameDuplicate(@RequestParam String nickname) {
        boolean isDuplicate = userService.isNicknameDuplicate(nickname);
        return ResponseEntity.ok().body(Map.of("isDuplicate", isDuplicate));
    }


    @PostMapping("/refresh")
    public ResponseEntity<?> refreshAccessToken(@RequestHeader("Authorization") String refreshTokenHeader) {
        String newAccessToken = authService.refreshAccessToken(refreshTokenHeader);
        return ResponseEntity.ok().body(Map.of("accessToken", newAccessToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestHeader("X-Refresh-Token") String refreshToken) {

        authService.logout(userDetails.getUser(), refreshToken);
        return ResponseEntity.ok(Map.of("message", "로그아웃 완료"));
    }


}
