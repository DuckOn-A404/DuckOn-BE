package com.a404.duckonback.domain.room.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LiveRoomDTO {
    private Long roomId;
    private String title;
    private Long artistId;
    private String hostId;
    private String hostNickname;
    private String imgUrl;
    private List<String> playlist;
    private int currentVideoIndex;
    private double currentTime;

    @JsonProperty("playing")
    private boolean playing;

    private long lastUpdated;

    @JsonProperty("locked")
    private boolean locked;

    private String entryQuestion;
    private String entryAnswer;
    private long participantCount;


}
