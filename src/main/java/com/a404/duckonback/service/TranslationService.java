package com.a404.duckonback.service;

import com.a404.duckonback.config.ServiceProperties;
import com.a404.duckonback.dto.TranslateRequest;
import com.a404.duckonback.dto.TranslateResponse;
import com.a404.duckonback.util.LanguageDetector;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
public class TranslationService {

    private static final Logger log = LoggerFactory.getLogger(TranslationService.class);

    private final RestClient translateRestClient;
    private final ServiceProperties props;

    /** 프론트 요구사항에 맞춘 간단 API: message + tgt만 받으면 src는 자동감지 */
    public String translateAutoDetect(String message, String targetLang) {
        if (message == null || message.isBlank()) return "";
        String src = LanguageDetector.detect(message);
        return translateBlocking(message, src, targetLang, true);
    }

    /** ChatMapper 등 내부에서 직접 호출할 수 있는 풀옵션 버전 */
    public String translateBlocking(String text, String src, String tgt, boolean useGlossary) {
        if (text == null || text.isBlank()) return "";
        String normSrc = normLang(src);
        String normTgt = normLang(tgt);

        // 같은 언어면 그대로 반환
        if (normSrc != null && normSrc.equalsIgnoreCase(normTgt)) {
            return text;
        }

        try {
            TranslateRequest req = new TranslateRequest(text, normSrc, normTgt, useGlossary);
            TranslateResponse res = translateRestClient.post()
                    .uri("/translate")
                    .body(req)
                    .retrieve()
                    .body(TranslateResponse.class);

            if (res == null || res.translation() == null) {
                log.warn("TranslateResponse is null/empty. text={}, src={}, tgt={}", text, normSrc, normTgt);
                return text; // 혹은 ""
            }
            return res.translation();

        } catch (Exception e) {
            log.warn("Translate call failed: {}", e.toString());
            return text; // 실패 시 원문 반환(UX 고려)
        }
    }

    /** 파이썬 서버와 같은 규칙으로 언어코드 정규화 */
    private String normLang(String code) {
        if (code == null || code.isBlank()) return null;
        String x = code.toLowerCase();
        return switch (x) {
            case "zh-cn", "zh_cn", "zh-hans", "zh_hans" -> "zh";
            case "zh-tw", "zh_tw", "zh-hant", "zh_hant" -> "zh";
            case "pt-br", "pt_br" -> "pt";
            default -> x;
        };
    }
}
