package com.a404.duckonback.controller;

import com.a404.duckonback.dto.SendEmailCodeRequest;
import com.a404.duckonback.dto.SendEmailCodeResponse;
import com.a404.duckonback.dto.VerifyEmailCodeRequest;
import com.a404.duckonback.dto.VerifyEmailCodeResponse;
import com.a404.duckonback.service.EmailVerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "이메일 인증", description = "이메일 인증번호 발송 및 검증")
@RestController
@RequestMapping("/api/auth/email")
@RequiredArgsConstructor
public class EmailVerificationController{

    private final EmailVerificationService emailVerificationService;

    @Operation(summary = "이메일 인증번호 발송")
    @PostMapping("/code")
    public ResponseEntity<SendEmailCodeResponse> sendCode(@Valid @RequestBody SendEmailCodeRequest req) {
        emailVerificationService.sendCode(req.getEmail(), req.getEmailPurpose());
        return ResponseEntity.ok(new SendEmailCodeResponse(true, "인증번호를 전송했습니다."));
    }

    @Operation(summary = "이메일 인증번호 검증")
    @PostMapping("/verify")
    public ResponseEntity<VerifyEmailCodeResponse> verify(@Valid @RequestBody VerifyEmailCodeRequest req) {
        boolean ok = emailVerificationService.verifyCode(req.getEmail(), req.getEmailPurpose(), req.getCode());
        return ResponseEntity.ok(new VerifyEmailCodeResponse(ok, ok ? "인증 성공" : "인증 실패"));
    }
}
