package com.a404.duckonback.dto;

import com.a404.duckonback.enums.RoomSyncEventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LiveRoomSyncDTO {
    private RoomSyncEventType eventType;

    private Long roomId;
    private String hostId;
    private String hostNickname;
    private java.util.List<String> playlist;
    private int currentVideoIndex;
    private double currentTime;
    private boolean playing;
    private long lastUpdated;
}
