package com.a404.duckonback.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomPresenceDTO {
    private Long roomId;
    private long participantCount;
}
