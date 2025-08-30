package com.a404.duckonback.controller;

import com.a404.duckonback.dto.LoginRequestDTO;
import com.a404.duckonback.dto.SignupRequestDTO;
import com.a404.duckonback.exception.CustomException;
import com.a404.duckonback.filter.CustomUserPrincipal;
import com.a404.duckonback.service.AuthService;
import com.a404.duckonback.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Tag(name = "인증 관리", description = "로그인, 회원가입, 이메일 중복 확인 등의 인증 관련 기능을 제공합니다.")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @Operation(summary = "로그인", description = "사용자가 이메일과 비밀번호로 로그인합니다. 성공 시 JWT 토큰을 반환합니다.")
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO loginRequest){
        return ResponseEntity.ok(authService.login(loginRequest));
    }

//    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다. 프로필 사진을 포함한 회원가입을 지원합니다.")
//    @PostMapping(value = "/signup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public ResponseEntity<?> signup(@ModelAttribute SignupRequestDTO request) {
//        return ResponseEntity.ok(authService.signup(request));
//    }
    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다. 프로필 사진을 포함한 회원가입을 지원합니다.")
    @PostMapping(value = "/signup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> signup(@Valid @ModelAttribute SignupRequestDTO request,
                                    BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            FieldError fe = bindingResult.getFieldErrors().getFirst();
            String msg = fe.getDefaultMessage();
            if (msg == null || msg.isBlank()) {
                msg = "필수 항목을 모두 입력해주세요.";
            }
            throw new CustomException(msg, HttpStatus.BAD_REQUEST);
        }

        if (request.getNickname() == null || request.getNickname().trim().isEmpty()) {
            throw new CustomException("닉네임을 입력해주세요", HttpStatus.BAD_REQUEST);
        }

        if(request.getNickname().getBytes(StandardCharsets.UTF_8).length > 24){
            throw new CustomException(
                    "닉네임은 최대 24바이트(영문 24자/한글 8자)까지 가능합니다.",
                    HttpStatus.BAD_REQUEST
            );
        }

        return ResponseEntity.ok(authService.signup(request));
    }

    @Operation(summary = "이메일 중복 확인", description = "사용자가 입력한 이메일이 이미 등록되어 있는지 확인합니다.")
    @GetMapping("/email/exists")
    public ResponseEntity<?> checkEmailDuplicate(@RequestParam String email) {
        boolean isDuplicate = userService.isEmailDuplicate(email);
        return ResponseEntity.ok().body(Map.of("isDuplicate", isDuplicate));
    }

    @Operation(summary = "사용자 ID 중복 확인", description = "사용자가 입력한 사용자 ID가 이미 등록되어 있는지 확인합니다.")
    @GetMapping("/user-id/exists")
    public ResponseEntity<?> checkUserIdDuplicate(@RequestParam String userId) {
        boolean isDuplicate = userService.isUserIdDuplicate(userId);
        return ResponseEntity.ok().body(Map.of("isDuplicate", isDuplicate));
    }

    @Operation(summary = "닉네임 중복 확인", description = "사용자가 입력한 닉네임이 이미 등록되어 있는지 확인합니다.")
    @GetMapping("/nickname/exists")
    public ResponseEntity<?> checkNicknameDuplicate(@RequestParam String nickname) {
        boolean isDuplicate = userService.isNicknameDuplicate(nickname);
        return ResponseEntity.ok().body(Map.of("isDuplicate", isDuplicate));
    }

    @Operation(summary = "토큰 갱신", description = "리프레시 토큰을 사용하여 새로운 액세스 토큰, 리프레시 토큰을 발급받습니다.")
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshJWT(@RequestHeader("Authorization") String refreshTokenHeader) {
        Map<String, String> tokenMap = authService.refreshJWT(refreshTokenHeader);
        return ResponseEntity.ok(tokenMap);
    }

    @Operation(summary = "로그아웃", description = "사용자가 로그아웃합니다. 리프레시 토큰을 무효화합니다.")
    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestHeader(value = "X-Refresh-Token", required = false) String refreshToken
    ) {
        authService.logout(principal.getUser(), refreshToken);
        return ResponseEntity.ok(Map.of("message", "로그아웃 완료"));
    }


}
