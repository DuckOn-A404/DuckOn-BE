package com.a404.duckonback.dto;

import com.a404.duckonback.entity.ChatMessage;
import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;

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
