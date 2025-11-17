package com.a404.duckonback.controller;

import com.a404.duckonback.dto.ChatMessageDTO;
import com.a404.duckonback.dto.LiveRoomDTO;
import com.a404.duckonback.dto.LiveRoomSyncDTO;
import com.a404.duckonback.dto.UserRankDTO;
import com.a404.duckonback.entity.User;
import com.a404.duckonback.enums.RoomSyncEventType;
import com.a404.duckonback.exception.CustomException;
import com.a404.duckonback.service.RedisService;
import com.a404.duckonback.service.UserRankService;
import com.a404.duckonback.util.ChatRateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.time.Duration;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class RoomSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final RedisService redisService;
    private final ChatRateLimiter chatRateLimiter;
    private final UserRankService userRankService;

    // 영상 동기화 메시지
    @Operation(summary = "방 내 영상 동기화 메시지 (JWT 필요X)")
    @MessageMapping("/room/update")
    public void updateRoom(@Payload LiveRoomSyncDTO dto,
                           StompHeaderAccessor accessor) {

        User user = (User) accessor.getSessionAttributes().get("user");

        if (user == null) {
            throw new CustomException("사용자 없음", HttpStatus.NOT_FOUND);
        }

        if (!user.getUserId().equals(dto.getHostId())) {
            throw new CustomException("호스트만 변경할 수 있습니다.", HttpStatus.FORBIDDEN);
        }

        dto.setEventType(RoomSyncEventType.SYNC_STATE);

        redisService.updateRoomInfo(dto);
        messagingTemplate.convertAndSend("/topic/room/" + dto.getRoomId(), dto);
    }

    // 채팅 메시지 수신
    @Operation(summary = "방 내 채팅 (JWT 필요X)")
    @MessageMapping("/room/chat")
    public void chat(@Payload ChatMessageDTO message,
                     StompHeaderAccessor accessor) {

        User user = (User) accessor.getSessionAttributes().get("user");
        Boolean isGuest = (Boolean) accessor.getSessionAttributes().getOrDefault("guest", Boolean.FALSE);

        if(!message.isImage() &&message.getContent().length() > 100){
            throw new CustomException("채팅은 100자 이하만 가능합니다.", HttpStatus.BAD_REQUEST);
        }

        String rateKey;
        if (user != null && !isGuest) {
            message.setSenderId(user.getUserId());
            message.setSenderNickName(user.getNickname());
            message.setSenderLang(user.getLanguage()); // 로그인 유저 언어 세팅
            UserRankDTO userRankDTO = userRankService.getUserRank(user.getId());
            message.setUserRank(userRankDTO);
            rateKey = chatRateLimiter.userKey(user.getUserId());
        } else {
            // 게스트
            String guestId = (String) accessor.getSessionAttributes().get("guestId");
            String guestNickname = (String) accessor.getSessionAttributes().get("guestNickname");
            String guestLang = (String) accessor.getSessionAttributes().get("guestLang");

            if (guestId == null) guestId = "guest:" + java.util.UUID.randomUUID();
            if (guestNickname == null) guestNickname = "게스트";
            if (guestLang == null) guestLang = "en"; // 기본 언어는 영어로

            message.setSenderId(guestId);            // 문자열 ID 허용(프론트 필터링에도 사용됨)
            message.setSenderNickName(guestNickname);
            message.setSenderLang(guestLang);

            rateKey = chatRateLimiter.userKey(guestId); // 같은 레이트리밋 로직 재사용
        }

        boolean allowed = chatRateLimiter.allow(rateKey, 5, Duration.ofSeconds(5));
        if (!allowed) {
            throw new CustomException(
                    "채팅은 5초에 5번까지만 가능합니다.",
                    HttpStatus.TOO_MANY_REQUESTS,
                    Map.of("type","CHAT_RATE_LIMITED")
            );
        }

        messagingTemplate.convertAndSend("/topic/chat/" + message.getRoomId(), message);
    }
}