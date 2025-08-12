package com.a404.duckonback.controller;

import com.a404.duckonback.dto.ChatMessageDTO;
import com.a404.duckonback.dto.LiveRoomDTO;
import com.a404.duckonback.dto.LiveRoomSyncDTO;
import com.a404.duckonback.entity.User;
import com.a404.duckonback.enums.RoomSyncEventType;
import com.a404.duckonback.exception.CustomException;
import com.a404.duckonback.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class RoomSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final RedisService redisService;

    // 영상 동기화 메시지
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
    @MessageMapping("/room/chat")
    public void chat(@Payload ChatMessageDTO message,
                     StompHeaderAccessor accessor) {

        User user = (User) accessor.getSessionAttributes().get("user");
        if (user != null) {
            message.setSenderNickName(user.getNickname()); // 또는 userId
            message.setSenderId(user.getUserId());
        } else {
            throw new CustomException("로그인이 필요합니다.", HttpStatus.NOT_FOUND);
        }

        if(message.getContent().length() > 100){
            throw new CustomException("채팅은 100자 이하만 가능합니다.", HttpStatus.BAD_REQUEST);
        }

        boolean allowed = redisService.increaseChatCount(
                message.getRoomId().toString(),
                user.getUserId()
        );
        if (!allowed) {
            throw new CustomException("채팅은 5초에 10번까지만 가능합니다.", HttpStatus.TOO_MANY_REQUESTS);
        }



        messagingTemplate.convertAndSend("/topic/chat/" + message.getRoomId(), message);
    }
}