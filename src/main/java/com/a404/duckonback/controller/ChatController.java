package com.a404.duckonback.controller;

import com.a404.duckonback.dto.ChatMessageRequestDTO;
import com.a404.duckonback.dto.ChatMessageResponseDTO;
import com.a404.duckonback.dto.UserRankDTO;
import com.a404.duckonback.entity.ChatMessage;
import com.a404.duckonback.exception.CustomException;
import com.a404.duckonback.service.ChatService;
import com.a404.duckonback.filter.CustomUserPrincipal;
import com.a404.duckonback.service.UserRankService;
import com.a404.duckonback.util.ChatRateLimiter;
import io.swagger.v3.oas.annotations.Operation;
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
    private final UserRankService userRankService;

    /**
     * 클라이언트가 메시지를 보낼 때 호출하는 엔드포인트
     * POST /api/chat/artist/{artistId}/message
     */
    @Operation(summary = "채팅 메시지 전송 (JWT 필요O)", description = "아티스트 창에서 채팅 메시지를 전송합니다.")
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

        ChatMessageResponseDTO responseDTO = ChatMessageResponseDTO.fromEntity(saved);
        UserRankDTO rankDTO = userRankService.getUserRank(principal.getUser().getId());

        responseDTO.setUserRank(rankDTO);
        return ResponseEntity.ok(responseDTO);
    }

    /**
     * 클라이언트가 주기적으로 채팅 내역을 조회할 때 호출
     * GET /api/chat/artist/{artistId}/message
     */
    @Operation(
            summary = "아티스트 채팅 메시지 조회 (JWT 필요X)",
            description = "아티스트 창에서 채팅 메시지 내역을 조회합니다. 선택적으로 특정 시점 이후의 메시지만 조회할 수 있습니다."
    )
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
