package com.a404.duckonback.dto;

import lombok.Getter;
import lombok.Builder;
import java.util.List;

@Getter
@Builder
public class HomeArtistRoomDTO {
    private Long artistId;
    private String artistName;
    private String artistImgUrl;
    private List<LiveRoomSummaryDTO> rooms;
}
