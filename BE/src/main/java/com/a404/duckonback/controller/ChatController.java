package com.a404.duckonback.controller;

import com.a404.duckonback.dto.ChatMessageRequestDTO;
import com.a404.duckonback.dto.ChatMessageResponseDTO;
import com.a404.duckonback.entity.ChatMessage;
import com.a404.duckonback.exception.CustomException;
import com.a404.duckonback.service.ChatService;
import com.a404.duckonback.filter.CustomUserPrincipal;
import com.a404.duckonback.util.ChatRateLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;
    private final ChatRateLimiter chatRateLimiter;

    /**
     * 클라이언트가 메시지를 보낼 때 호출하는 엔드포인트
     * POST /api/chat/artist/{artistId}/message
     */
    @PostMapping("/artist/{artistId}/message")
    public ResponseEntity<ChatMessageResponseDTO> sendMessage(
            @PathVariable String artistId,
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestBody ChatMessageRequestDTO dto
    ) {
        if(dto.getContent().length() > 100){
            throw new CustomException("채팅은 100자 이하만 가능합니다.", HttpStatus.BAD_REQUEST);
        }

        String key = chatRateLimiter.userKey(principal.getUser().getUserId());
        boolean allowed = chatRateLimiter.allow(key, 5, Duration.ofSeconds(5));
//        if (!allowed) {
//            throw new CustomException("채팅은 5초에 5번까지만 가능합니다.", HttpStatus.TOO_MANY_REQUESTS);
//        }
        if (!allowed) {
            throw new CustomException(
                    "채팅은 5초에 5번까지만 가능합니다.",
                    HttpStatus.TOO_MANY_REQUESTS,
                    Map.of(
                            "type", "CHAT_RATE_LIMITED"
                    ));
        }

        ChatMessage saved = chatService.sendMessage(
                principal.getUser().getId(),
                artistId,
                dto
        );
        return ResponseEntity.ok(ChatMessageResponseDTO.fromEntity(saved));
    }

    /**
     * 클라이언트가 주기적으로 채팅 내역을 조회할 때 호출
     * GET /api/chat/artist/{artistId}/message
     */
    @GetMapping("/artist/{artistId}/message")
    public ResponseEntity<List<ChatMessageResponseDTO>> getMessage(
            @PathVariable String artistId,
            @RequestParam(required = false) Instant since
    ) {
        List<ChatMessageResponseDTO> history = (since == null)
                ? chatService.getHistory(artistId)
                : chatService.getHistorySince(artistId, since);
        return ResponseEntity.ok(history);
    }

}
