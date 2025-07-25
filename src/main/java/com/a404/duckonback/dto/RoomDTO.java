package com.a404.duckonback.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomDTO {
    private Long roomId;
    private String title;
    private String imgUrl;
    private LocalDateTime createdAt;
    private String creatorId;
    private int artistId;
}
