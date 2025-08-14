package com.a404.duckonback.util;

import com.a404.duckonback.entity.User;
import com.a404.duckonback.service.TranslationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatMapper {

    private final TranslationService translationService;

    public record ChatPayload(String original, String translated, String src, String tgt) {}

    public ChatPayload toPayloadFor(User receiver, String messageText, String messageLang) {
        String tgt = receiver.getLanguage(); // 예: "en", "ko", "ja"...
        if (tgt == null || tgt.isBlank() || tgt.equalsIgnoreCase(messageLang)) {
            return new ChatPayload(messageText, null, messageLang, tgt);
        }
        // 글로서리 ON: 아이돌 용어 고정
        String translated = translationService.translateBlocking(messageText, messageLang, tgt, true);
        return new ChatPayload(messageText, translated, messageLang, tgt);
    }
}
