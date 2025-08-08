package com.a404.duckonback.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageDTO {
    private Long roomId;
    private String senderId;
    private String senderNickName;
    private String content;
    @Builder.Default
    private Instant sentAt = Instant.now();
}
