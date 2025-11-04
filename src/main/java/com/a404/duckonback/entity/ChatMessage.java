package com.a404.duckonback.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDateTime;

@Document(collection = "artist_chats")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@CompoundIndexes({
        @CompoundIndex(name = "idx_artist_sentAt", def = "{\"artistId\": 1, \"sentAt\": 1}")
})
public class ChatMessage {
    @Id
    private String id;

    private String artistId;        // 어느 아티스트 페이지 채팅인지

    @Indexed(name = "idx_senderId")
    private Long senderId;          // 보낸 사람 PK
    private String senderUserId;    // 보낸 사람의 userId
    private String senderNickname;  // 보낸 사람의 nickname
    private String content;         // 텍스트 내용
    private Instant sentAt;   // 보낸 시각
}
