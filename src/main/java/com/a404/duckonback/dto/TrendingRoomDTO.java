package com.a404.duckonback.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrendingRoomDTO {
    private Long roomId;
    private Long artistId;
    private String artistNameEn;
    private String artistNameKr;
    private String title;
    private String hostId;
    private String hostNickname;
    private String hostProfileImgUrl;
    private String imgUrl;
    private int participantCount;
}
