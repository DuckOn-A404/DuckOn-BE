// com.a404.duckonback.dto.LiveRoomSyncDTO
package com.a404.duckonback.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LiveRoomSyncDTO {
    private Long roomId;
    private String hostId;
    private java.util.List<String> playlist;
    private int currentVideoIndex;
    private double currentTime;
    private boolean isPlaying;
    private long lastUpdated;
}
