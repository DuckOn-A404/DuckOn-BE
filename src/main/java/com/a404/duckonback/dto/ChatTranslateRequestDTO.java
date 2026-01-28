package com.a404.duckonback.dto;

import lombok.Data;

@Data
public class ChatTranslateRequestDTO {
    /** 번역할 원문 텍스트 */
    private String text;

    /** 보낸 사람의 설정 언어. 예: "es" */
    private String senderLang;

    /**
     * 클라이언트가 지정한 타겟 언어.
     * - 로그인 유저면 TranslationService 에서 DB 언어가 우선
     * - 비로그인 유저면 이 값 또는 Accept-Language 로 결정
     */
    private String targetLang;
}
