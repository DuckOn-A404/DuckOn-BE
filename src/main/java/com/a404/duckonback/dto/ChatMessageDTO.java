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
    private String content; // isImage == true -> url / false -> String(일반 메시지)
    private boolean isImage; // 해당 dto의 content가 이미지 url 인지, 일반 메세지(String) 인지
    @Builder.Default
    private Instant sentAt = Instant.now();
}
