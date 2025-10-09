package com.a404.duckonback.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDateTime;

@Document(collection = "subject_chats")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ChatMessage {
    @Id
    private String id;

    private String subjectId;        // 어느 상세 페이지 채팅인지
    private Long senderId;          // 보낸 사람 PK
    private String senderUserId;    // 보낸 사람의 userId
    private String senderNickname;  // 보낸 사람의 nickname
    private String content;         // 텍스트 내용
    private Instant sentAt;   // 보낸 시각
}
