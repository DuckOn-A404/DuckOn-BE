package com.a404.duckonback.domain.youtube.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class YoutubeSearchItemDTO {
    String videoId;
    String title;
    String channelTitle;
    String thumbnailUrl;
}