package com.a404.duckonback.chat;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ChatMessageDTO {
    private String artistId;
    private Long senderId;
    private String senderName;
    private String content;
}
