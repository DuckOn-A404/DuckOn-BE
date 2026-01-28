package com.a404.duckonback.domain.chat.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ChatMessageRequestDTO {
    private String content;
}
