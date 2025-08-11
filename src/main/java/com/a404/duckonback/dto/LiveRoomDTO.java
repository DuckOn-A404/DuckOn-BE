package com.a404.duckonback.dto;

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
    private boolean playing;
    private long lastUpdated;
    private boolean isLocked;
    private String entryQuestion;
    private String entryAnswer;
    private long participantCount;


}
