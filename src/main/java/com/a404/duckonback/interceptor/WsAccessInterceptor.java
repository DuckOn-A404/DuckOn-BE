package com.a404.duckonback.interceptor;

import com.a404.duckonback.entity.User;
import com.a404.duckonback.exception.CustomException;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@Component
public class WsAccessInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor acc = StompHeaderAccessor.wrap(message);

        if (StompCommand.SEND.equals(acc.getCommand())) {
            String dest = acc.getDestination();
            if (dest == null) return message;

            // 송신 제한: 로그인 사용자만 허용
            if (dest.equals("/app/room/chat") || dest.equals("/app/room/update")) {
                User user = (User) acc.getSessionAttributes().get("user");
                if (user == null) {
                    throw new CustomException("로그인이 필요합니다.", HttpStatus.UNAUTHORIZED);
                }
            }
        }

        // SUBSCRIBE는 제한X (모두 수신 가능)
        return message;
    }
}