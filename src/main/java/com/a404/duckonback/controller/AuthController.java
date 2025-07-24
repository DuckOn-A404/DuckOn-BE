package com.a404.duckonback.controller;

import com.a404.duckonback.dto.LoginRequestDTO;
import com.a404.duckonback.dto.SignupRequestDTO;
import com.a404.duckonback.service.AuthService;
import com.a404.duckonback.service.AuthServiceImpl;
import com.a404.duckonback.service.UserService;
import com.a404.duckonback.service.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

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

//    @PostMapping("/refresh")
//    public ResponseEntity<?> refreshAccessToken(@RequestHeader("Authorization") String refreshTokenHeader) {
//        // Bearer 제거
//        if (!refreshTokenHeader.startsWith("Bearer ")) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("잘못된 형식의 토큰입니다.");
//        }
//
//        String refreshToken = refreshTokenHeader.substring(7);
//
//        try {
//            String newAccessToken = authService.refreshAccessToken(refreshToken);
//            return ResponseEntity.ok().body(Map.of("accessToken", newAccessToken));
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
//        }
//    }


}
