package com.a404.duckonback.controller;

import com.a404.duckonback.dto.ChatTranslateRequestDTO;
import com.a404.duckonback.dto.ChatTranslateResponseDTO;
import com.a404.duckonback.filter.CustomUserPrincipal;
import com.a404.duckonback.response.ApiResponseDTO;
import com.a404.duckonback.response.SuccessCode;
import com.a404.duckonback.service.TranslationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "번역", description = "채팅/텍스트 번역 기능을 제공합니다.")
@RestController
@RequestMapping("/api/translation")
@RequiredArgsConstructor
public class TranslationController {

    private final TranslationService translationService;

    @Operation(
            summary = "채팅 메시지 번역 (JWT 필요X)",
            description = """
                    텍스트와 언어 정보를 받아 로그인/비로그인 유저에 맞게 번역합니다.
                    - 로그인 유저: DB에 저장된 선호 언어를 타겟 언어로 사용
                    - 비로그인 유저: 요청의 targetLang 또는 브라우저의 Accept-Language 헤더를 사용
                    """
    )
    @PostMapping("/chat")
    public ResponseEntity<ApiResponseDTO<ChatTranslateResponseDTO>> translateChatMessgae(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestBody ChatTranslateRequestDTO request,
            HttpServletRequest httpServletRequest
    ){
        ChatTranslateResponseDTO result = translationService.translateChatMessage(principal, request, httpServletRequest);
        return ResponseEntity.ok(ApiResponseDTO.success(SuccessCode.CHAT_TRANSLATION_SUCCESS, result));
    }
}
