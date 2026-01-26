package com.a404.duckonback.domain.youtube.dto;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class YoutubeSearchResponseDTO {
    List<YoutubeSearchItemDTO> items;

}
