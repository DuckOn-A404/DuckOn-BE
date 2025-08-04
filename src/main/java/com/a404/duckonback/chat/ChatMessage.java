package com.a404.duckonback.chat;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "artist_chats")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ChatMessage {
    @Id
    private String id;

    private String artistId;      // 어느 아티스트 페이지 채팅인지
    private Long senderId;        // 보낸 사람 PK
    private String senderName;    // 닉네임 등 표시용
    private String content;       // 텍스트 내용
    private LocalDateTime sentAt; // 보낸 시각
}
