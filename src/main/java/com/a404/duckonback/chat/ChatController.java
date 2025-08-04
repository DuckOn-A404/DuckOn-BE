package com.a404.duckonback.chat;

import com.a404.duckonback.oauth.principal.CustomUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;

    /**
     * 클라이언트가 메시지를 보낼 때 호출하는 엔드포인트
     * POST /api/chat/artist/{artistId}/message
     */
    @PostMapping("/artist/{artistId}/message")
    public ResponseEntity<ChatMessage> sendMessage(
            @PathVariable String artistId,
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestBody ChatMessageDTO dto
    ) {
        // 유저 인증 정보가 필요 없으면 @AuthenticationPrincipal 은 생략 가능
        dto.setArtistId(artistId);
        dto.setSenderId(principal.getUser().getId());
        dto.setSenderName(principal.getUser().getNickname());
        ChatMessage saved = chatService.save(dto);
        return ResponseEntity.ok(saved);
    }

    /**
     * 클라이언트가 주기적으로 채팅 내역을 조회할 때 호출
     * GET /api/chat/artist/{artistId}/message
     */
    @GetMapping("/artist/{artistId}/message")
    public ResponseEntity<List<ChatMessage>> getMessage(
            @PathVariable String artistId,
            @RequestParam(required = false) LocalDateTime since  // ▲ 옵션: 이후 메시지만 조회
    ) {
        List<ChatMessage> history = (since == null)
                ? chatService.getHistory(artistId)
                : chatService.getHistorySince(artistId, since);
        return ResponseEntity.ok(history);
    }

}
