package com.a404.duckonback.dto;

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