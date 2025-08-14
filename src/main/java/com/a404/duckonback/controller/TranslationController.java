package com.a404.duckonback.controller;

import com.a404.duckonback.dto.FrontTranslateRequest;
import com.a404.duckonback.dto.FrontTranslateResponse;
import com.a404.duckonback.service.TranslationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class TranslationController {

    private final TranslationService translationService;

    @PostMapping("/translate")
    public ResponseEntity<FrontTranslateResponse> translate(@RequestBody FrontTranslateRequest payload) {
        if (payload == null || !StringUtils.hasText(payload.message()) || !StringUtils.hasText(payload.language())) {
            return ResponseEntity.badRequest().build();
        }
        String translated = translationService.translateAutoDetect(payload.message(), payload.language());
        return ResponseEntity.ok(new FrontTranslateResponse(translated));
    }
}
