package com.a404.duckonback.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatTranslateResponseDTO {
    private String originalText;
    private String sourceLang; // 감지된 실제 언어
    private String targetLang; // 최종 번역 언어
    private String translatedText; // 번역 결과
}
