package com.a404.duckonback.controller;

import com.a404.duckonback.dto.MemeCreateRequestDTO;
import com.a404.duckonback.dto.SignupRequestDTO;
import com.a404.duckonback.filter.CustomUserPrincipal;
import com.a404.duckonback.response.ApiResponseDTO;
import com.a404.duckonback.service.MemeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "밈 관리", description = "")
@RestController
@RequestMapping("/api/meme")
@RequiredArgsConstructor
public class MemeController {

    private final MemeService memeService;

    @Operation(summary = "밈 생성(저장)", description = "새로운 밈을 등록합니다. 최대 3개의 밈을 등록할 수 있으며 각 밈에는 다수의 태그가 부여될 수 있습니다.")
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponseDTO<ResponseEntity<?>> create(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @ModelAttribute MemeCreateRequestDTO request) {
        try {
            Long userId = principal.getId();
            memeService.createMeme(userId, request);

            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 내부 오류가 발생했습니다.");
        }
    }
}
