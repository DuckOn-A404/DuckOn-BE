package com.a404.duckonback.common.config;

import com.a404.duckonback.domain.user.entity.User;
import com.a404.duckonback.common.interceptor.JwtHandshakeInterceptor;
import com.a404.duckonback.common.interceptor.WsAccessInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;


@RequiredArgsConstructor
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtHandshakeInterceptor jwtHandshakeInterceptor;
    private final WsAccessInterceptor wsAccessInterceptor;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-chat")
                .setHandshakeHandler(new DefaultHandshakeHandler() {
                    @Override
                    protected Principal determineUser(ServerHttpRequest request,
                                                      WebSocketHandler wsHandler,
                                                      Map<String, Object> attributes) {
                        // JwtHandshakeInterceptor 가 넣어둔 값 사용
                        User user = (User) attributes.get("user");
                        if (user != null) return new StompPrincipal(user.getUserId());

                        // 게스트 허용 케이스
                        String guestId = (String) attributes.get("guestId");
                        if(guestId != null) return new StompPrincipal(guestId);

                        return null;
                    }
                })
                .addInterceptors(jwtHandshakeInterceptor)
                .setAllowedOriginPatterns("*")
        ;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/app");
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(wsAccessInterceptor);
    }


}