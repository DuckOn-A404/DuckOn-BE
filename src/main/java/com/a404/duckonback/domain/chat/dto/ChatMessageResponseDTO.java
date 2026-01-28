package com.a404.duckonback.domain.chat.dto;

import com.a404.duckonback.domain.chat.entity.ChatMessage;
import com.a404.duckonback.domain.user.dto.UserRankDTO;
import lombok.*;

import java.time.Instant;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ChatMessageResponseDTO {
    private String messageId;     // MongoDB ObjectId
    private String userId;        // User.userId (not PK)
    private String userNickname;  // User.nickname
    private String content;
    private Instant sentAt;
    private UserRankDTO userRank;

    public static ChatMessageResponseDTO fromEntity(ChatMessage e) {
        return ChatMessageResponseDTO.builder()
                .messageId(e.getId())
                .userId(e.getSenderUserId())
                .userNickname(e.getSenderNickname())
                .content(e.getContent())
                .sentAt(e.getSentAt())
                .build();
    }
}
