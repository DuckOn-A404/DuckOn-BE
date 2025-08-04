package com.a404.duckonback.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;
    private final SimpMessagingTemplate template;

    // 클라이언트가 /app/chat/artist.send 로 메시지 보냄
    @MessageMapping("/chat/artist.send")
    public void onMessage(@Payload ChatMessageDTO dto) {
        // 1) MongoDB에 저장
        ChatMessage saved = chatService.save(dto);

        // 2) 같은 아티스트 페이지 구독자들에게 전송
        template.convertAndSend(
                "/topic/artist/" + dto.getArtistId(),
                saved
        );
    }

    // 과거 대화 불러오기용 API (선택)
    @GetMapping("/api/chat/artist/{artistId}/history")
    public List<ChatMessage> history(@PathVariable String artistId) {
        return chatService.getHistory(artistId);
    }
}
