package com.a404.duckonback.domain.auth.controller;

import com.a404.duckonback.common.dto.*;
import com.a404.duckonback.common.exception.CustomException;
import com.a404.duckonback.common.filter.CustomUserPrincipal;
import com.a404.duckonback.common.notification.email.EmailVerificationService;
import com.a404.duckonback.common.response.ApiResponseDTO;
import com.a404.duckonback.common.response.ErrorCode;
import com.a404.duckonback.common.response.SuccessCode;
import com.a404.duckonback.domain.auth.dto.RefreshResponseDTO;
import com.a404.duckonback.domain.auth.service.AuthService;
import com.a404.duckonback.domain.auth.dto.LoginRequestDTO;
import com.a404.duckonback.domain.auth.dto.SignupRequestDTO;
import com.a404.duckonback.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@Tag(name = "인증 관리", description = "로그인, 회원가입, 이메일 중복 확인 등의 인증 관련 기능을 제공합니다.")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final EmailVerificationService emailVerificationService;

    @Operation(summary = "로그인 (JWT 필요X)", description = "사용자가 이메일과 비밀번호로 로그인합니다. 성공 시 JWT 토큰을 반환합니다.")
    @PostMapping("/login")
    public void login(@RequestBody LoginRequestDTO loginRequest){
//        return ResponseEntity.ok(authService.login(loginRequest));
    }

    @Operation(summary = "회원가입 (JWT 필요X)", description = "새로운 사용자를 등록합니다. 프로필 사진을 포함한 회원가입을 지원합니다.")
    @PostMapping(value = "/signup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> signup(@ModelAttribute SignupRequestDTO request) {
        try {
            return authService.signup(request);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 내부 오류가 발생했습니다.");
        }
    }

    @Operation(summary = "이메일 중복 확인 (JWT 필요X)", description = "사용자가 입력한 이메일이 이미 등록되어 있는지 확인합니다.")
    @GetMapping("/email/exists")
    public ResponseEntity<?> checkEmailDuplicate(@RequestParam String email) {
        boolean isDuplicate = userService.isEmailDuplicate(email);
        return ResponseEntity.ok().body(Map.of("isDuplicate", isDuplicate));
    }

    @Operation(summary = "사용자 ID 중복 확인 (JWT 필요X)", description = "사용자가 입력한 사용자 ID가 이미 등록되어 있는지 확인합니다.")
    @GetMapping("/user-id/exists")
    public ResponseEntity<?> checkUserIdDuplicate(@RequestParam String userId) {
        boolean isDuplicate = userService.isUserIdDuplicate(userId);
        return ResponseEntity.ok().body(Map.of("isDuplicate", isDuplicate));
    }

    @Operation(summary = "닉네임 중복 확인 (JWT 필요X)", description = "사용자가 입력한 닉네임이 이미 등록되어 있는지 확인합니다.")
    @GetMapping("/nickname/exists")
    public ResponseEntity<?> checkNicknameDuplicate(@RequestParam String nickname) {
        boolean isDuplicate = userService.isNicknameDuplicate(nickname);
        return ResponseEntity.ok().body(Map.of("isDuplicate", isDuplicate));
    }

    @Operation(summary = "토큰 갱신 (JWT 필요X)", description = "HttpOnly 쿠키의 refreshToken으로 access token을 재발급합니다.")
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponseDTO<RefreshResponseDTO>> refreshJWT(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        RefreshResponseDTO dto = authService.refreshJWT(request, response);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessCode.AUTH_REFRESH_SUCCESS, dto));
    }

    @Operation(summary = "로그아웃 (JWT 필요O)", description = "access 블랙리스트 처리 + refreshToken 쿠키 삭제")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponseDTO<Void>> logout(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        authService.logout(principal.getUser(), request, response);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessCode.AUTH_LOGOUT_SUCCESS));
    }

    @Operation(summary = "이메일 인증번호 발송 (JWT 필요X)", description = "싸피 와이파이로 작동하지 않습니다. 꼭 핫스팟으로 실행해주세요!")
    @PostMapping("/code")
    public ResponseEntity<SendEmailCodeResponse> sendCode(@Valid @RequestBody SendEmailCodeRequest req) {
        emailVerificationService.sendCode(req.getEmail(), req.getEmailPurpose());
        return ResponseEntity.ok(new SendEmailCodeResponse(true, "인증번호를 전송했습니다."));
    }

    @Operation(summary = "이메일 인증번호 검증 (JWT 필요X)")
    @PostMapping("/verify")
    public ResponseEntity<VerifyEmailCodeResponse> verify(@Valid @RequestBody VerifyEmailCodeRequest req) {
        boolean ok = emailVerificationService.verifyCode(req.getEmail(), req.getEmailPurpose(), req.getCode());
        return ResponseEntity.ok(new VerifyEmailCodeResponse(ok, ok ? "인증 성공" : "인증 실패"));
    }


    @Operation(summary = "이메일 인증번호 검증 for 비밀번호 찾기 : JWT 발급 (JWT 필요X)")
    @PostMapping("/verify-with-token")
    public ResponseEntity<ApiResponseDTO<JWTDTO>> verifyForPassword(@Valid @RequestBody VerifyEmailCodeRequest req) {
        boolean ok = emailVerificationService.verifyCode(req.getEmail(), req.getEmailPurpose(), req.getCode());
        if(ok){
            JWTDTO response = authService.getJWT(req.getEmail());
            return ResponseEntity.ok(ApiResponseDTO.success(SuccessCode.EMAIL_VERIFIED, response));

        }
        throw new CustomException(ErrorCode.EMAIL_VERIFY_FAILED);
    }

}
