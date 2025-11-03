package com.a404.duckonback.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class RoomSummaryDTO {
    private Long roomId;
    private String title;
    private String imgUrl;
    private LocalDateTime createdAt;
}
