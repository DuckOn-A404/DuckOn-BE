package com.a404.duckonback.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageDTO {
    private Long roomId;
    private String senderId;
    private String senderNickName;
    private String content;
    private long timestamp;
}
