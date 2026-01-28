package com.a404.duckonback.dto;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class YoutubeSearchResponseDTO {
    List<YoutubeSearchItemDTO> items;

}
