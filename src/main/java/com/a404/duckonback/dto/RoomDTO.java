package com.a404.duckonback.dto;

import com.a404.duckonback.entity.Room;
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
    private Long artistId;
    private String artistNameEn;
    private String artistNameKr;

    public static RoomDTO fromEntity(Room room) {
        return RoomDTO.builder()
                .roomId(room.getRoomId())
                .title(room.getTitle())
                .imgUrl(room.getImgUrl())
                .createdAt(room.getCreatedAt())
                .creatorId(room.getCreator().getUserId())
                .artistId(room.getArtist().getArtistId())
                .artistNameEn(room.getArtist().getNameEn())
                .artistNameKr(room.getArtist().getNameKr())
                .build();
    }
}
