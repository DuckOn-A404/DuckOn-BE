package com.a404.duckonback.service;

import com.a404.duckonback.config.ServiceProperties;
import com.a404.duckonback.dto.ChatTranslateRequestDTO;
import com.a404.duckonback.dto.ChatTranslateResponseDTO;
import com.a404.duckonback.entity.User;
import com.a404.duckonback.exception.CustomException;
import com.a404.duckonback.filter.CustomUserPrincipal;
import com.a404.duckonback.response.ErrorCode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;


@Slf4j
@Service
@RequiredArgsConstructor
public class TranslationService {

    private final ServiceProperties serviceProperties;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;
//    private final LanguageDetector languageDetector;

    /**
     * 채팅 메시지 번역
     * - 로그인/비로그인 여부
     * - 타겟 언어 결정
     * - OpenAI 호출 및 오류 처리
     * 전부 이 메서드 안에서 처리
     */
    public ChatTranslateResponseDTO translateChatMessage(
            CustomUserPrincipal principal,
            ChatTranslateRequestDTO request,
            HttpServletRequest httpRequest
    ){
        // 1. 요청 검증
        if(request == null || !StringUtils.hasText(request.getText())){
            throw new CustomException(ErrorCode.TRANSLATION_TEXT_EMPTY);
        }
        String originalText = request.getText().trim();
        String targetLang = resolveTargetLang(principal, request.getTargetLang(), httpRequest);

        // 2. OpenAI Chat Completions 호출 준비
        String apiKey = serviceProperties.getOpenAiApiKey();
        String model = serviceProperties.getOpenAiModel();

        RestClient client = RestClient.builder()
                .baseUrl("https://api.openai.com/v1/chat/completions")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

        String systemPrompt = """
                You are a translation engine.
                1. Detect the actual language of the given text.
                2. Translate the text into the target language.
                3. If the text is already in the target language,return the original text.
                4. Respond ONLY as a JSON object with the fields:
                    { "sourceLang": "<iso_639_1>", "translatedText": "<text>" }
                """;

        Map<String, Object> userPayload = Map.of(
                "text", originalText,
                "targetLang", targetLang,
                "senderLangHint", request.getSenderLang()
        );

        Map<String, Object> body;
        try {
            body = Map.of(
                    "model", model,
                    "messages", List.of(
                            Map.of("role", "system", "content", systemPrompt),
                            Map.of("role", "user", "content", objectMapper.writeValueAsString(userPayload))
                    ),
                    "response_format", Map.of("type", "json_object")
            );
        } catch (Exception e) {
            log.error("OpenAI 요청 body 생성 중 오류", e);
            throw new CustomException("번역 요청 생성 중 오류가 발생했습니다.", ErrorCode.TRANSLATION_FAILED);
        }

        try {
            JsonNode root = client.post()
                    .body(body)
                    .retrieve()
                    .body(JsonNode.class);

            String content = root.path("choices")
                    .path(0)
                    .path("message")
                    .path("content")
                    .asText(null);

            if (!StringUtils.hasText(content)) {
                log.error("OpenAI 응답에 content 없음: {}", root);
                throw new CustomException("번역 응답이 올바르지 않습니다.", ErrorCode.TRANSLATION_FAILED);
            }

            JsonNode data = objectMapper.readTree(content);
            String sourceLang = data.path("sourceLang").asText(null);
            String translatedText = data.path("translatedText").asText(null);

            if (!StringUtils.hasText(sourceLang) || !StringUtils.hasText(translatedText)) {
                log.error("OpenAI 응답 필드 누락: {}", content);
                throw new CustomException("번역 응답이 올바르지 않습니다.", ErrorCode.TRANSLATION_FAILED);
            }

            return ChatTranslateResponseDTO.builder()
                    .originalText(originalText)
                    .sourceLang(sourceLang)
                    .targetLang(targetLang)
                    .translatedText(translatedText)
                    .build();

        } catch (RestClientException e) {
            log.error("OpenAI 번역 API 호출 실패", e);
            throw new CustomException("번역 중 외부 API 호출에 실패했습니다.", ErrorCode.TRANSLATION_FAILED);
        } catch (Exception e) {
            log.error("번역 처리 중 알 수 없는 오류", e);
            throw new CustomException("번역 처리 중 오류가 발생했습니다.", ErrorCode.TRANSLATION_FAILED);
        }
    }


    /**
     * 타겟 언어 결정 우선순위
     * 1) 로그인 유저의 선호 언어 (DB)
     * 2) 요청 본문에 명시된 targetLang
     * 3) 브라우저 Accept-Language 헤더
     * 4) 기본값 "en"
     */
    private String resolveTargetLang(
            CustomUserPrincipal principal,
            String targetLangFromRequest,
            HttpServletRequest httpServletRequest
    ){
        // 1. 로그인 유저
        if(principal != null){
            User user = principal.getUser();
            if(user != null && StringUtils.hasText(user.getLanguage())){
                return normalizeLangCode(user.getLanguage());
            }
        }

        // 2. 요청에서 보낸 targetLang
        if(StringUtils.hasText(targetLangFromRequest)){
            return normalizeLangCode(targetLangFromRequest);
        }

        // 3. Accept-Language 헤더
        String header = httpServletRequest.getHeader(HttpHeaders.ACCEPT_LANGUAGE);
        log.info("Translation target language not found, header language : {}", header);
        if(StringUtils.hasText(header)){
            // 예: "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7"
            String first = header.split(",")[0];     // "ko-KR"
            String lang = first.split(";")[0];       // "ko-KR"
            String shortCode = lang.split("-")[0];   // "ko"
            return normalizeLangCode(shortCode);
        }

        // 4. 최후는 영어
        return "en";
    }

    private String normalizeLangCode(String lang){
        if(lang == null) return null;
        return lang.trim().toLowerCase();
    }

}
